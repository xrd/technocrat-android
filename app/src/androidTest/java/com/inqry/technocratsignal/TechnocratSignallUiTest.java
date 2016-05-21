package com.inqry.technocratsignalandroid;


import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.inqry.technocratandroid.CustomView;
import com.inqry.technocratandroid.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by xrdawson on 2/7/16.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TechnocratSignallUiTest {


    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Before
    public void initValidString() {
        // Specify a valid string.
        //mStringToBetyped = "Espresso";
    }

    @Test
    public void checkCoordinates() {
        CustomView bs = (CustomView)mActivityRule.getActivity().findViewById(R.id.technocratsignal);
        bs.setTechnocratSignalState( false, "Just some string" );
        bs.setDeltaX( 10 );
        bs.setDeltaY( 10 );
        // Now we should have X and Y where?
        bs.update();
    }
}


