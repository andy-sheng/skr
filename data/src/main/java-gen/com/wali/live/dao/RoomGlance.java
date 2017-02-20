package com.wali.live.dao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table ROOM_GLANCE.
 */
public class RoomGlance implements java.io.Serializable {

    private Long id;
    private Long uuid;
    private String roomId;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public RoomGlance() {
    }

    public RoomGlance(Long id) {
        this.id = id;
    }

    public RoomGlance(Long id, Long uuid, String roomId) {
        this.id = id;
        this.uuid = uuid;
        this.roomId = roomId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUuid() {
        return uuid;
    }

    public void setUuid(Long uuid) {
        this.uuid = uuid;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
