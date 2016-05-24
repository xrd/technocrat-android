package com.inqry.technocratsignal;

import java.util.HashMap;

/**
 * Created by chrx on 3/9/16.
 */
public interface IStatusServer {

//    static void setStatusServer( StatusServerImpl ss );
//
//    static StatusServer getServer();

void setUrl( String url );

    boolean test();

    boolean isInitialized();

    HashMap<String,Boolean> poll();

}
