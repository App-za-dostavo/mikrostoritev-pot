package si.fri.rso.path.api.v1;

import com.kumuluz.ee.discovery.annotations.RegisterService;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@RegisterService(value = "pot-service", ttl = 20, pingInterval = 15, environment = "test", version = "1.0.0", singleton = false)
@OpenAPIDefinition(info = @Info(title = "Pot API", version = "v1",
        license = @License(name = "dev")),
        servers = @Server(url = "http://localhost:8081/"))
@ApplicationPath("/v1")
public class PathApplication extends Application {
}
