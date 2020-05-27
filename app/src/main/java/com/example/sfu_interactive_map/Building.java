package com.example.sfu_interactive_map;

import java.util.ArrayList;
import java.util.List;

public class Building {
    //which group does building belong to
    enum Group{
        OTHERS,
        STUDENT_AREA,
        NONE
    }
    //Full name of building
    String bld_name;
    //Abbreviation of building name
    String abbr;
    //The building code (may not need to be used)
    String bld_code;
    //Number of stories of building
    int levels;
    //What is special about this building
    String bld_attrib;
    //all rooms belonging to this bld
    List<Room> rooms;
    //all floors
    List<Integer> all_levels;
    //http request for overview building
    String url_overview;
    //http request for all rooms belonging to this building
    String url_allrooms;
    public Building(){
        bld_name = "";
        abbr = "";
        bld_code = "";
        levels = -1;
        bld_attrib = "";
        rooms = new ArrayList<>();
        all_levels = new ArrayList<>();
        url_overview = "";
        url_allrooms = "";
    }
    public Building(
            String bld_name,
            String abbr,
            String bld_code,
            int levels,
            String bld_attrib,
            List<Room> rooms,
            List<Integer> all_levels,
            String url_overview,
            String url_allrooms
            )
    {
        this.bld_name = bld_name;
        this.abbr = abbr;
        this.bld_code = bld_code;
        this.levels = levels;

    }
}
