package com.example.jhuang.newanimenotification;

public class Model {
    /**
     * the position of the item in the list
     */
    private int position;
    private String animeName;

    public String getAnimeName(){
        return this.animeName;
    }

    public void setAnimeName(String animeName){
        this.animeName = animeName;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private boolean isChecked;

    public boolean getChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}