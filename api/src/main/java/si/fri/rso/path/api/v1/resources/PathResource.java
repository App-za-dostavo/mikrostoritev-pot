package si.fri.rso.path.api.v1.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;
import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import com.kumuluz.ee.cors.annotations.CrossOrigin;
import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.logs.cdi.Log;
import com.kumuluz.ee.logs.cdi.LogParams;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import si.fri.rso.path.lib.DeliveryPerson;
import si.fri.rso.path.lib.Order;
import si.fri.rso.path.lib.OrderList;
import si.fri.rso.path.services.beans.OrderListBean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Log(LogParams.METRICS)
@ConfigBundle("kumuluzee.external-api")
@ApplicationScoped
@javax.ws.rs.Path("/pot")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@CrossOrigin(supportedMethods = "GET, POST, PUT, HEAD, DELETE, OPTIONS")
public class PathResource {

    private static final double LATITUDE = 46.0466531;
    private static final double LONGITUDE = 14.5076098;
    private String url = "http://172.21.0.5:8082/v1/dostavljalec";
    @Inject
    private OrderListBean orderListBean;

    @Context
    protected UriInfo uriInfo;

    @Inject
    @DiscoverService(value = "dostavljalec-service", environment = "dev", version = "1.0.0")
    private Optional<String> dostavljalecUrl;

    @ConfigValue(watch = true)
    private String mapquestApi = "https://www.mapquestapi.com/directions/v2/routematrix?key=C0a6ATPbuQ30XjwFnoy7xAYYmajidgtD";

    public String getMapquestApi() {
        return mapquestApi;
    }

    public void setMapquestApi(String mapquestApi) {
        this.mapquestApi = mapquestApi;
    }

    CloseableHttpClient httpClient = HttpClients.createDefault();
    ObjectMapper mapper = new ObjectMapper();

    @Operation(description = "Get an ip of another microservice", summary = "Get method for fetching ip from a different service.")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "Delivery person ip",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @APIResponse(responseCode = "500", description = "Service unavailable")

    })
    @GET
    @javax.ws.rs.Path("/dostavljalec")
    @Produces("application/json")
    public Response getDostavljalecIp() {

        System.out.println("Ip is " + dostavljalecUrl);
        return Response.status(Response.Status.OK).entity(dostavljalecUrl).build();
    }

    @Operation(description = "Get a list of available orders", summary = "Orders list")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "List of orders",
                    content = @Content(schema = @Schema(implementation = DeliveryPerson.class)))})
    @GET
    @Log(value = LogParams.METRICS)
    public Response getOrderList() {

        List<OrderList> orderLists = orderListBean.getOrders(uriInfo);

        return Response.status(Response.Status.OK).entity(orderLists).build();
    }

    @Operation(description = "Post a new order with included data about time and distance needed", summary = "New order with time, distance and delivery person included")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "Time and distance list",
                    content = @Content(schema = @Schema(implementation = DeliveryPerson.class))),
            @APIResponse(responseCode = "500", description = "Something went wrong")
    })
    @POST
    @Path("/lokacija={latitude},{longitude}")
    public Response getDeliveryDiscovery(@PathParam("latitude") Double latitude, @PathParam("longitude") Double longitude,
                                         @RequestBody(description = "A new order added to the database", required = true,
                                                 content = @Content(schema = @Schema(implementation = Order.class)))
                                         Order order) throws JsonProcessingException {
        if (!dostavljalecUrl.isPresent()) {
            System.out.println("Other service unavailable");
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        java.util.List<DeliveryPerson> deliveryPersonList = null;
        java.util.List<DeliveryPerson> availablePeopleList = new ArrayList<>();


        // Get a list of delivery people
        String deliveryPersonString = myHttpGet(url);

        try {
            deliveryPersonList = mapper.readValue(deliveryPersonString, new TypeReference<java.util.List<DeliveryPerson>>() {
            });
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

        for (int i = 0; i < deliveryPersonList.size(); i++) {
            if (deliveryPersonList.get(i).getAvailability()) {
                availablePeopleList.add(deliveryPersonList.get(i));
                String jsonInStringPretty = mapper.
                        writerWithDefaultPrettyPrinter().writeValueAsString(availablePeopleList.get(i));
                System.out.println("Available people:  " + jsonInStringPretty);
            }
        }

        // Call external api
        for (int i = 0; i < availablePeopleList.size(); i++) {

            //Create a json object
            JSONObject jsonBody = new JSONObject();
            JSONArray latLngArr = new JSONArray();

            // Add the location of the person ordering
            JSONObject currLatLng = new JSONObject().put("latLng", new JSONObject()
                    .put("lat", latitude).put("lng", longitude));
            latLngArr.put(currLatLng);

            // Add the location of one restaurant
            JSONObject restaurantLocation = new JSONObject()
                    .put("latLng", new JSONObject().put("lat", LATITUDE).put("lng", LONGITUDE));
            latLngArr.put(restaurantLocation);

            // Add the location of the delivery person
            JSONObject latLng = new JSONObject().put("latLng", new JSONObject()
                    .put("lat", availablePeopleList.get(i).getLatitude())
                    .put("lng", availablePeopleList.get(i).getLongitude()));
            latLngArr.put(latLng);
            jsonBody.put("locations", latLngArr);

            System.out.println("Mapquestapi " + mapquestApi);
            System.out.println("Body " + jsonBody);
            String apiResponse = myHttpPost(mapquestApi, jsonBody.toString());

            JSONObject mapQuestJson;
            JSONArray distances;
            JSONArray times;

            try {
                mapQuestJson = new JSONObject(apiResponse);
                System.out.println("MapquestJSON" + mapQuestJson);
                distances = (JSONArray) mapQuestJson.get("distance");
                System.out.println("Distance " + distances);
                times = (JSONArray) mapQuestJson.get("time");
            } catch (JSONException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Something went wrong while parsing JSON response").build();
            }

            DeliveryPerson person = availablePeopleList.get(i);
            Double distance = 0.0;
            Integer time = 0;
            for (int j = 1; j < distances.length(); j++) {
                distance += (Double) (distances.get(j));
                time += (Integer) (times.get(j));
            }
            distance = distance*1.609344;
            person.setDistance(distance);
            person.setTime(time);
        }

        DeliveryPerson personWithLowestTime = availablePeopleList.stream().min(Comparator.comparing(DeliveryPerson::getTime)).orElseThrow(NoSuchElementException::new);

        OrderList orderList = new OrderList();
        orderList.setTime(personWithLowestTime.getTime());
        orderList.setDistance(personWithLowestTime.getDistance());
        orderList.setFirstName(personWithLowestTime.getFirstName());
        orderList.setLastName(personWithLowestTime.getLastName());
        orderList.setItems(order.getItems());
        orderList.setCost(order.getCost());

        OrderList finalList = orderListBean.createOrder(orderList);

        // Change availability
        personWithLowestTime.setAvailability(false);
        String putBodyString = null;

        try {
            putBodyString = mapper.writeValueAsString(personWithLowestTime);
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Problem while delivery person object.").build();
        }

        myHttpPut(url + "/" + personWithLowestTime.getId(), putBodyString);

        return Response.status(Response.Status.OK).entity(finalList).build();
    }

    @Operation(description = "Delete an order from a list", summary = "Delete order")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Order successfully deleted."
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Not found."
            )
    })
    @DELETE
    @Path("/{orderId}")
    public Response deleteOrder(@Parameter(description = "Order id") @PathParam("orderId") Integer orderId) {

        boolean deleted = orderListBean.deleteOrder(orderId);

        if (deleted) {
            return Response.status(Response.Status.OK).entity("Successfully deleted order id" + orderId).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("This order does not exist").build();
        }
    }

    private String myHttpPost(String url, String jsonbody) {
        HttpPost request = new HttpPost(url);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        CloseableHttpResponse response = null;
        try {
            request.setEntity(new StringEntity(jsonbody));
        } catch (UnsupportedEncodingException e) {
            return e.getMessage();
        }
        try {
            response = httpClient.execute(request);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException | IllegalArgumentException e) {
            return e.getMessage();
        }

    }

    private String myHttpGet(String url) {
        HttpGet request = new HttpGet(url);
        CloseableHttpResponse response = null;

        try {
            response = httpClient.execute(request);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private String myHttpPut(String url, String jsonbody) {
        HttpPut request = new HttpPut(url);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        CloseableHttpResponse response = null;
        System.out.println(jsonbody);
        try {
            request.setEntity(new StringEntity(jsonbody));
            System.out.println(request.getEntity().toString());
        } catch (UnsupportedEncodingException e) {
            return e.getMessage();
        }
        try {
            response = httpClient.execute(request);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException | IllegalArgumentException e) {
            return  e.getMessage();
        }
    }
}
