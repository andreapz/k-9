package com.fsck.k9.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Annalisa Sini on 21/11/2016.
 */

public class NavDrawerMenuItem implements Serializable{
    private static final long serialVersionUID = 4054223372059735340L;

    private String title;
    private String section_id;
    private String url;
    private String ico;
    private boolean fav;
    private boolean visibility;
    private boolean customizable;
    private List<NavDrawerMenuItem> sections = new ArrayList<>();

    public NavDrawerMenuItem() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSectionId() {
        return section_id;
    }

    public void setSectionId(String sectionId) {
        this.section_id = sectionId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean getFav() {
        return fav;
    }

    public void setFav(boolean fav) {
        this.fav = fav;
    }

    public boolean isVisible() {
        return visibility;
    }

    public void setVisible(boolean visibility) {
        this.visibility = visibility;
    }

    public String getIconUrl() {
        return ico;
    }

    public void setIconUrl(String iconUrl) {
        this.ico = iconUrl;
    }


    public List<NavDrawerMenuItem> getSections() {
        return sections;
    }

    public void setSections(List<NavDrawerMenuItem> sections) {
        this.sections.clear();
        this.sections.addAll(sections);
    }

    public boolean isCustomizable() {
        return customizable;
    }

    public void setCustomizable(boolean customizable) {
        this.customizable = customizable;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof NavDrawerMenuItem) {
            NavDrawerMenuItem menuItem = (NavDrawerMenuItem) obj;
            if(menuItem.getSectionId() != null) {
                return this.getSectionId().equals(menuItem.getSectionId());
            }
        }
        return false;
    }

    public static NavDrawerMenuItem getMenuItem(JSONObject obj) {
        if(obj != null) {
            NavDrawerMenuItem menuItem = new NavDrawerMenuItem();
            menuItem.setTitle(obj.optString("title", null));
            menuItem.setSectionId(obj.optString("section_id", null));
            menuItem.setUrl(obj.optString("url", null));
            menuItem.setIconUrl(obj.optString("ico", null));
            menuItem.setVisible(obj.optBoolean("visibility", true));
            menuItem.setFav(obj.optBoolean("fav", false));
            JSONArray sectionsJson = obj.optJSONArray("sections");
            if(sectionsJson != null) {
                List<NavDrawerMenuItem> sections = new ArrayList<>();
                for(int j=0; j<sectionsJson.length(); j++) {
                    JSONObject sectionObj = sectionsJson.optJSONObject(j);
                    if(sectionObj != null) {
                        NavDrawerMenuItem sectionItem = getMenuItem(sectionObj);
                        sections.add(sectionItem);
                    }
                }
                menuItem.setSections(sections);
            }
            return menuItem;
        }
        return null;
    }

    public static List<NavDrawerMenuItem> getMenuList(String meObjectJsonString, String key) {
        try {
            JSONObject meObjectJson = new JSONObject(meObjectJsonString);
            JSONObject meJsonObject = meObjectJson.getJSONObject("me");
            JSONObject tabJsonObject = meJsonObject.getJSONObject(key);
            JSONArray menuJsonArray = tabJsonObject.getJSONArray("menu");
            List<NavDrawerMenuItem> menuList = new ArrayList<>();
            for (int i=0; i<menuJsonArray.length(); i++) {
                JSONObject obj = menuJsonArray.getJSONObject(i);
                NavDrawerMenuItem menuItem = getMenuItem(obj);
                menuList.add(menuItem);
            }
            return menuList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
