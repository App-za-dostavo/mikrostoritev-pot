package si.fri.rso.path.models.entities;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "pot")
@NamedQueries(value =
        {
                @NamedQuery(name = "OrderListEntity.getAll", query = "SELECT order FROM OrderListEntity order"),
                @NamedQuery(name = "OrderListEntity.getById", query = "SELECT order FROM OrderListEntity order WHERE order.id=:id")
        })
public class OrderListEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String items;
    private Double cost;
    private Integer time;
    private Double distance;
    private String firstName;
    private String lastName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
