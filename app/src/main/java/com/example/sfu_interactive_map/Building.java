package com.example.sfu_interactive_map;

import java.util.ArrayList;
import java.util.HashMap;
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
    //Building description
    String bld_descr;
    //all floors belonging to this bld
    HashMap<Integer, Floor> floors;
    //http request for overview building
    String url_building;

    public Building(){
        bld_name = "";
        abbr = "";
        bld_code = "";
        bld_descr = "";
        floors = null;
        url_building = "";
    }
    public Building(
            String bld_name,
            String abbr,
            String bld_code,
            String bld_descr,
            String url_building
            )
    {
        this.bld_name = bld_name;
        this.abbr = abbr;
        this.bld_code = bld_code;
        this.bld_descr = bld_descr;
        this.url_building = url_building;
        this.floors = new HashMap<Integer, Floor>();
    }
}
