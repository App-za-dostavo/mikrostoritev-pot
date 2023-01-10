package si.fri.rso.path.api.v1.health;


import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;
import java.net.HttpURLConnection;
import java.net.URL;

@Readiness
@ApplicationScoped
@ConfigBundle("external-api")
public class DostavljalecHealthCheck implements HealthCheck {

    @ConfigValue(watch = true)
    private String dostavljalecApi = "http://172.21.0.5:8082/v1/dostavljalec";

    @Override
    public HealthCheckResponse call() {

        if(dostavljalecApi == null)  {
            System.out.println("its null");
            return HealthCheckResponse.named(DostavljalecHealthCheck.class.getSimpleName()).down().build();
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(dostavljalecApi).openConnection();
            connection.setRequestMethod("HEAD");

            if (connection.getResponseCode() == 200) {
                return HealthCheckResponse.named(DostavljalecHealthCheck.class.getSimpleName()).up().build();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return HealthCheckResponse.named(DostavljalecHealthCheck.class.getSimpleName()).down().build();
    }

}
