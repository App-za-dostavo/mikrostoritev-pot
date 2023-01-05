package si.fri.rso.path.models.converters;

import si.fri.rso.path.lib.OrderList;
import si.fri.rso.path.models.entities.OrderListEntity;

public class OrderListConverter {

    public static OrderList toDto(OrderListEntity entity) {

        OrderList dto = new OrderList();
        dto.setId(entity.getId());
        dto.setItems(entity.getItems());
        dto.setCost(entity.getCost());
        dto.setTime(entity.getTime());
        dto.setDistance(entity.getDistance());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());

        return dto;
    }

    public static OrderListEntity toEntity(OrderList dto) {

        OrderListEntity entity = new OrderListEntity();
        entity.setId(dto.getId());
        entity.setItems(dto.getItems());
        entity.setCost(dto.getCost());
        entity.setTime(dto.getTime());
        entity.setDistance(dto.getDistance());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());

        return entity;
    }

}
