/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * ONLINE MODE
 * This activity is currently not in use.
 */
public class OnlineChat extends AppCompatActivity {

    private Toolbar mToolbar;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_chat);

        mToolbar = findViewById(R.id.chatapp_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Smart EduBox Chat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Tabs
        mViewPager = (ViewPager) findViewById(R.id.chatOnline_tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.chatOnline_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.chatapp_menu, menu);

        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.chatapp_menu_userInfo:
                Intent goToAllUsers = new Intent(this, OnlineChatUsers.class);
                startActivity(goToAllUsers);
                return true;
        }
        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "No signed-in user. Please sign in first.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, OnlineLogin.class);
            startActivity(intent);
            finish();
        }
    }

}