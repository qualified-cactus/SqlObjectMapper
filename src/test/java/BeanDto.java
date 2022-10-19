import sqlObjectMapper.Column;
import sqlObjectMapper.LeftJoinedMany;
import sqlObjectMapper.LeftJoinedOne;

import java.util.List;


public class BeanDto {


    public static class Order {

        @Column(isId = true)
        private Integer itemId;
        private String itemName;
        @LeftJoinedOne
        private PlacedOrder placedOrder;

        public Integer getItemId() {
            return itemId;
        }

        public void setItemId(Integer itemId) {
            this.itemId = itemId;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public PlacedOrder getPlacedOrder() {
            return placedOrder;
        }

        public void setPlacedOrder(PlacedOrder placedOrder) {
            this.placedOrder = placedOrder;
        }
    }

    public static class PlacedOrder {
        @Column(isId = true)
        private Integer orderId;
        private String orderName;
        @LeftJoinedMany
        private List<TargetLocation> locations;

        public Integer getOrderId() {
            return orderId;
        }

        public void setOrderId(Integer orderId) {
            this.orderId = orderId;
        }

        public String getOrderName() {
            return orderName;
        }

        public void setOrderName(String orderName) {
            this.orderName = orderName;
        }

        public List<TargetLocation> getLocations() {
            return locations;
        }

        public void setLocations(List<TargetLocation> locations) {
            this.locations = locations;
        }
    }

    public static class TargetLocation {
        @Column(isId = true)
        private Integer locationId;
        private String address;

        public Integer getLocationId() {
            return locationId;
        }

        public void setLocationId(Integer locationId) {
            this.locationId = locationId;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
}
