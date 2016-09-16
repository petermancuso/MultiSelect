package io.gforce.templateMasterDetail;

import java.util.Date;
import java.util.UUID;

// Model Layer containing information about a Record

public class Record {
    private UUID mId;
    private Date mDate;
    private boolean mCheck0;
    private String mContact;
    private String mTitle;
    private int mPosition;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public boolean isCheck0() {
        return mCheck0;
    }

    public void setCheck0(boolean check0) {
        mCheck0 = check0;
    }

    public String getContact(){
        return mContact;
    }
    public void setContact(String contact){
        mContact = contact;
    }
    public String getPhotoFilename() {
        return "IMG_" + getId().toString() + ".jpg";
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }


    public Record() {
        //calls constructor below
        this(UUID.randomUUID());
    }

    public Record(UUID id) {
        mId = id;
        mDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }
}