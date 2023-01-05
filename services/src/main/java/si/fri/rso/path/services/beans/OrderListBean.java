package si.fri.rso.path.services.beans;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import si.fri.rso.path.lib.OrderList;
import si.fri.rso.path.models.converters.OrderListConverter;
import si.fri.rso.path.models.entities.OrderListEntity;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Collectors;

@RequestScoped
public class OrderListBean {

    @Inject
    private EntityManager em;

    public List<OrderList> getOrders(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery()).defaultOffset(0).build();

        return JPAUtils.queryEntities(em, OrderListEntity.class, queryParameters).stream()
                .map(OrderListConverter::toDto).collect(Collectors.toList());
    }

    public OrderList createOrder(OrderList orderList) {

        OrderListEntity orderListEntity = OrderListConverter.toEntity(orderList);

        try {
            beginTx();
            em.persist(orderListEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        if (orderListEntity.getId() == null) {
            throw new RuntimeException("Entity was not persisted");
        }

        return OrderListConverter.toDto(orderListEntity);

    }

    public boolean deleteOrder(Integer id) {

        OrderListEntity orderListEntity = em.find(OrderListEntity.class, id);

        if (orderListEntity != null) {
            try {
                beginTx();
                em.remove(orderListEntity);
                commitTx();
            }
            catch (Exception e) {
                rollbackTx();
            }
        }
        else {
            return false;
        }

        return true;
    }

    private void beginTx() {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
    }

    private void commitTx() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().commit();
        }
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }

}
