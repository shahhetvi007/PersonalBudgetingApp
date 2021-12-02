package com.example.personalbudgetingapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
import com.example.personalbudgetingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class DailyAnalyticsActivity extends AppCompatActivity {

    private Toolbar settingsToolbar;

    private FirebaseAuth firebaseAuth;
    private String onlineUserId = "";
    private DatabaseReference expensesRef, personalRef;
    private AnyChartView anyChartView;

    private TextView totalBudgetAmountTextView, analyticsTransportAmount, analyticsFoodAmount, analyticsHouseAmount,
            analyticsEntAmount, analyticsEduAmount, analyticsApparelAmount, analyticsCharityAmount, analyticsHealthAmount,
            analyticsPersonalAmount, analyticsOtherAmount, dailySpentAmount;

    private RelativeLayout transportRL, foodRL, houseRL, entRL, eduRL, apparelRL, charityRL, healthRL, personalRL, otherRl;

    private TextView progress_ratio_transport, progress_ratio_food, progress_ratio_house, progress_ratio_ent, progress_ratio_edu,
            progress_ratio_apparel, progress_ratio_charity, progress_ratio_health, progress_ratio_personal, progress_ratio_other,
            dailyRatioSpending;

    private ImageView transport_status, food_status, house_status, ent_status, edu_status, apparel_status, charity_status,
            health_status, personal_status, other_status, dailyRatioSpendingIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_analytics);

        settingsToolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Today Analytics");

        firebaseAuth = FirebaseAuth.getInstance();
        onlineUserId = firebaseAuth.getCurrentUser().getUid();
        expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        personalRef = FirebaseDatabase.getInstance().getReference("personal").child(onlineUserId);

        totalBudgetAmountTextView = findViewById(R.id.totalBudgetAmountTextView);
        analyticsTransportAmount = findViewById(R.id.analyticsTransportAmount);
        analyticsFoodAmount = findViewById(R.id.analyticsFoodAmount);
        analyticsHouseAmount = findViewById(R.id.analyticsHouseAmount);
        analyticsEduAmount = findViewById(R.id.analyticsEduAmount);
        analyticsEntAmount = findViewById(R.id.analyticsEntAmount);
        analyticsApparelAmount = findViewById(R.id.analyticsApparelAmount);
        analyticsCharityAmount = findViewById(R.id.analyticsCharityAmount);
        analyticsHealthAmount = findViewById(R.id.analyticsHealthAmount);
        analyticsPersonalAmount = findViewById(R.id.analyticsPersonalAmount);
        analyticsOtherAmount = findViewById(R.id.analyticsOtherAmount);
        dailySpentAmount = findViewById(R.id.dailySpentAmount);

        transportRL = findViewById(R.id.transportRL);
        foodRL = findViewById(R.id.foodRL);
        healthRL = findViewById(R.id.healthRL);
        houseRL = findViewById(R.id.houseRL);
        eduRL = findViewById(R.id.eduRL);
        entRL = findViewById(R.id.entRL);
        apparelRL = findViewById(R.id.apparelRL);
        charityRL = findViewById(R.id.charityRL);
        personalRL = findViewById(R.id.personalRL);
        otherRl = findViewById(R.id.otherRL);

        progress_ratio_transport = findViewById(R.id.progress_ratio_transport);
        progress_ratio_food = findViewById(R.id.progress_ratio_food);
        progress_ratio_health = findViewById(R.id.progress_ratio_health);
        progress_ratio_house = findViewById(R.id.progress_ratio_house);
        progress_ratio_edu = findViewById(R.id.progress_ratio_edu);
        progress_ratio_ent = findViewById(R.id.progress_ratio_ent);
        progress_ratio_apparel = findViewById(R.id.progress_ratio_apparel);
        progress_ratio_charity = findViewById(R.id.progress_ratio_charity);
        progress_ratio_personal = findViewById(R.id.progress_ratio_personal);
        progress_ratio_other = findViewById(R.id.progress_ratio_other);
        dailyRatioSpending = findViewById(R.id.dailyRatioSpending);

        transport_status = findViewById(R.id.transport_status);
        food_status = findViewById(R.id.food_status);
        health_status = findViewById(R.id.health_status);
        house_status = findViewById(R.id.house_status);
        edu_status = findViewById(R.id.edu_status);
        ent_status = findViewById(R.id.ent_status);
        apparel_status = findViewById(R.id.apparel_status);
        charity_status = findViewById(R.id.charity_status);
        personal_status = findViewById(R.id.personal_status);
        other_status = findViewById(R.id.other_status);
        dailyRatioSpendingIV = findViewById(R.id.dailyRatioSpendingIV);

        anyChartView = findViewById(R.id.anyChartView);

        getTotalDailyTransportExpense();
        getTotalDailyFoodExpense();
        getTotalDailyHouseExpense();
        getTotalDailyEducationExpense();
        getTotalDailyEntertainmentExpense();
        getTotalDailyCharityExpense();
        getTotalDailyApparelExpense();
        getTotalDailyHealthExpense();
        getTotalDailyPersonalExpense();
        getTotalDailyOtherExpense();
        getTotalDaySpending();

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        loadGraph();
                        setStatusAndImageResource();
                    }
                }, 2000
        );
    }

    private void getTotalDailyTransportExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNDay = "Transport"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNDay").equalTo(itemNDay);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsTransportAmount.setText("Spent: " +totalAmount);
                    }
                    personalRef.child("dayTrans").setValue(totalAmount);
                } else {
                    transportRL.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyFoodExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNDay = "Food"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNDay").equalTo(itemNDay);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsFoodAmount.setText("Spent: " +totalAmount);
                    }
                    personalRef.child("dayFood").setValue(totalAmount);
                } else {
                    foodRL.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyHouseExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNDay = "House"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNDay").equalTo(itemNDay);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsHouseAmount.setText("Spent: " +totalAmount);
                    }
                    personalRef.child("dayHouse").setValue(totalAmount);
                } else {
                    houseRL.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyEducationExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNDay = "Education"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNDay").equalTo(itemNDay);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsEduAmount.setText("Spent: " +totalAmount);
                    }
                    personalRef.child("dayEducation").setValue(totalAmount);
                } else {
                    eduRL.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyEntertainmentExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNDay = "Entertainment"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNDay").equalTo(itemNDay);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsEntAmount.setText("Spent: " +totalAmount);
                    }
                    personalRef.child("dayEnt").setValue(totalAmount);
                } else {
                    entRL.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyCharityExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNDay = "Charity"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNDay").equalTo(itemNDay);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsCharityAmount.setText("Spent: " +totalAmount);
                    }
                    personalRef.child("dayCharity").setValue(totalAmount);
                } else {
                    charityRL.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyApparelExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNDay = "Apparel"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNDay").equalTo(itemNDay);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsApparelAmount.setText("Spent: " +totalAmount);
                    }
                    personalRef.child("dayApparel").setValue(totalAmount);
                } else {
                    apparelRL.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyHealthExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNDay = "Health"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNDay").equalTo(itemNDay);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsHealthAmount.setText("Spent: " +totalAmount);
                    }
                    personalRef.child("dayHealth").setValue(totalAmount);
                } else {
                    healthRL.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyPersonalExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNDay = "Personal"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNDay").equalTo(itemNDay);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsPersonalAmount.setText("Spent: " +totalAmount);
                    }
                    personalRef.child("dayPersonal").setValue(totalAmount);
                } else {
                    personalRL.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyOtherExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNDay = "Other"+date;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNDay").equalTo(itemNDay);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        analyticsOtherAmount.setText("Spent: " +totalAmount);
                    }
                    personalRef.child("dayOther").setValue(totalAmount);
                } else {
                    otherRl.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDaySpending(){
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("date").equalTo(date);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount()>0) {
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                    }
                    totalBudgetAmountTextView.setText("Total day's spending: Rs. " +totalAmount);
                    dailySpentAmount.setText("Total Spent: Rs "+totalAmount);
                } else {
                    totalBudgetAmountTextView.setText("You've not spent today");
                    anyChartView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGraph(){
        personalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int transTotal;
                    if (snapshot.hasChild("dayTrans")){
                        transTotal = Integer.parseInt(snapshot.child("dayTrans").getValue().toString());
                    } else {
                        transTotal = 0;
                    }

                    int foodTotal;
                    if (snapshot.hasChild("dayFood")){
                        foodTotal = Integer.parseInt(snapshot.child("dayFood").getValue().toString());
                    } else {
                        foodTotal = 0;
                    }

                    int houseTotal;
                    if (snapshot.hasChild("dayHouse")){
                        houseTotal = Integer.parseInt(snapshot.child("dayHouse").getValue().toString());
                    } else {
                        houseTotal = 0;
                    }

                    int entTotal;
                    if (snapshot.hasChild("dayEnt")){
                        entTotal = Integer.parseInt(snapshot.child("dayEnt").getValue().toString());
                    } else {
                        entTotal = 0;
                    }

                    int eduTotal;
                    if (snapshot.hasChild("dayEdu")){
                        eduTotal = Integer.parseInt(snapshot.child("dayEdu").getValue().toString());
                    } else {
                        eduTotal = 0;
                    }

                    int charityTotal;
                    if (snapshot.hasChild("dayCharity")){
                        charityTotal = Integer.parseInt(snapshot.child("dayCharity").getValue().toString());
                    } else {
                        charityTotal = 0;
                    }

                    int apparelTotal;
                    if (snapshot.hasChild("dayApparel")){
                        apparelTotal = Integer.parseInt(snapshot.child("dayApparel").getValue().toString());
                    } else {
                        apparelTotal = 0;
                    }

                    int healthTotal;
                    if (snapshot.hasChild("dayHealth")){
                        healthTotal = Integer.parseInt(snapshot.child("dayHealth").getValue().toString());
                    } else {
                        healthTotal = 0;
                    }

                    int personalTotal;
                    if (snapshot.hasChild("dayPersonal")){
                        personalTotal = Integer.parseInt(snapshot.child("dayPersonal").getValue().toString());
                    } else {
                        personalTotal = 0;
                    }

                    int otherTotal;
                    if (snapshot.hasChild("dayOther")){
                        otherTotal = Integer.parseInt(snapshot.child("dayOther").getValue().toString());
                    } else {
                        otherTotal = 0;
                    }

                    Pie pie = AnyChart.pie();
                    List<DataEntry> data = new ArrayList<>();
                    data.add(new ValueDataEntry("Transport", transTotal));
                    data.add(new ValueDataEntry("House", houseTotal));
                    data.add(new ValueDataEntry("Food", foodTotal));
                    data.add(new ValueDataEntry("Entertainment", entTotal));
                    data.add(new ValueDataEntry("Education", eduTotal));
                    data.add(new ValueDataEntry("Charity", charityTotal));
                    data.add(new ValueDataEntry("Apparel", apparelTotal));
                    data.add(new ValueDataEntry("Health", healthTotal));
                    data.add(new ValueDataEntry("Personal", personalTotal));
                    data.add(new ValueDataEntry("Other", otherTotal));

                    pie.data(data);
                    pie.title("Daily Analytics");
                    pie.labels().position("outside");

                    pie.legend().title().enabled(true);
                    pie.legend().title()
                            .text("Items Spent On")
                            .padding(0d, 0d, 10d, 0d);
                    pie.legend()
                            .position("center-bottom")
                            .itemsLayout(LegendLayout.HORIZONTAL)
                            .align(Align.CENTER);
                    anyChartView.setChart(pie);
                } else {
                    Toast.makeText(DailyAnalyticsActivity.this, "Child does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, "Child does not exist", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setStatusAndImageResource(){
        personalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    float transTotal;
                    if (snapshot.hasChild("dayTrans")) {
                        transTotal = Integer.parseInt(snapshot.child("dayTrans").getValue().toString());
                    } else {
                        transTotal = 0;
                    }

                    float foodTotal;
                    if (snapshot.hasChild("dayFood")) {
                        foodTotal = Integer.parseInt(snapshot.child("dayFood").getValue().toString());
                    } else {
                        foodTotal = 0;
                    }

                    float houseTotal;
                    if (snapshot.hasChild("dayHouse")) {
                        houseTotal = Integer.parseInt(snapshot.child("dayHouse").getValue().toString());
                    } else {
                        houseTotal = 0;
                    }

                    float entTotal;
                    if (snapshot.hasChild("dayEnt")) {
                        entTotal = Integer.parseInt(snapshot.child("dayEnt").getValue().toString());
                    } else {
                        entTotal = 0;
                    }

                    float eduTotal;
                    if (snapshot.hasChild("dayEdu")) {
                        eduTotal = Integer.parseInt(snapshot.child("dayEdu").getValue().toString());
                    } else {
                        eduTotal = 0;
                    }

                    float charityTotal;
                    if (snapshot.hasChild("dayCharity")) {
                        charityTotal = Integer.parseInt(snapshot.child("dayCharity").getValue().toString());
                    } else {
                        charityTotal = 0;
                    }

                    float apparelTotal;
                    if (snapshot.hasChild("dayApparel")) {
                        apparelTotal = Integer.parseInt(snapshot.child("dayApparel").getValue().toString());
                    } else {
                        apparelTotal = 0;
                    }

                    float healthTotal;
                    if (snapshot.hasChild("dayHealth")) {
                        healthTotal = Integer.parseInt(snapshot.child("dayHealth").getValue().toString());
                    } else {
                        healthTotal = 0;
                    }

                    float personalTotal;
                    if (snapshot.hasChild("dayPersonal")) {
                        personalTotal = Integer.parseInt(snapshot.child("dayPersonal").getValue().toString());
                    } else {
                        personalTotal = 0;
                    }

                    float otherTotal;
                    if (snapshot.hasChild("dayOther")) {
                        otherTotal = Integer.parseInt(snapshot.child("dayOther").getValue().toString());
                    } else {
                        otherTotal = 0;
                    }

                    float dailyTotalSpentAmount;
                    if (snapshot.hasChild("today")){
                        dailyTotalSpentAmount = Integer.parseInt(snapshot.child("today").getValue().toString());
                    } else {
                        dailyTotalSpentAmount = 0;
                    }


                    //Getting Ratios
                    float transRatio;
                    if (snapshot.hasChild("dayTransRatio")){
                        transRatio = Integer.parseInt(snapshot.child("dayTransRatio").getValue().toString());
                    } else {
                        transRatio = 0;
                    }

                    float foodRatio;
                    if (snapshot.hasChild("dayFoodRatio")){
                        foodRatio = Integer.parseInt(snapshot.child("dayFoodRatio").getValue().toString());
                    } else {
                        foodRatio = 0;
                    }

                    float houseRatio;
                    if (snapshot.hasChild("dayHouseRatio")){
                        houseRatio = Integer.parseInt(snapshot.child("dayHouseRatio").getValue().toString());
                    } else {
                        houseRatio = 0;
                    }

                    float entRatio;
                    if (snapshot.hasChild("dayEntRatio")){
                        entRatio = Integer.parseInt(snapshot.child("dayEntRatio").getValue().toString());
                    } else {
                        entRatio = 0;
                    }

                    float eduRatio;
                    if (snapshot.hasChild("dayEduRatio")){
                        eduRatio = Integer.parseInt(snapshot.child("dayEduRatio").getValue().toString());
                    } else {
                        eduRatio = 0;
                    }

                    float charityRatio;
                    if (snapshot.hasChild("dayCharRatio")){
                        charityRatio = Integer.parseInt(snapshot.child("dayCharRatio").getValue().toString());
                    } else {
                        charityRatio = 0;
                    }

                    float appRatio;
                    if (snapshot.hasChild("dayAppRatio")){
                        appRatio = Integer.parseInt(snapshot.child("dayAppRatio").getValue().toString());
                    } else {
                        appRatio = 0;
                    }

                    float healthRatio;
                    if (snapshot.hasChild("dayHealthRatio")){
                        healthRatio = Integer.parseInt(snapshot.child("dayHealthRatio").getValue().toString());
                    } else {
                        healthRatio = 0;
                    }

                    float perRatio;
                    if (snapshot.hasChild("dayPerRatio")){
                        perRatio = Integer.parseInt(snapshot.child("dayPerRatio").getValue().toString());
                    } else {
                        perRatio = 0;
                    }

                    float otherRatio;
                    if (snapshot.hasChild("dayOtherRatio")){
                        otherRatio = Integer.parseInt(snapshot.child("dayOtherRatio").getValue().toString());
                    } else {
                        otherRatio = 0;
                    }

                    float dailyTotalSpentAmountRatio;
                    if (snapshot.hasChild("dailyBudget")){
                        dailyTotalSpentAmountRatio = Integer.parseInt(snapshot.child("dailyBudget").getValue().toString());
                    } else {
                        dailyTotalSpentAmountRatio = 0;
                    }


                    //getting % and setting image
                    float dailyPercent = (dailyTotalSpentAmount/dailyTotalSpentAmountRatio) * 100;
                    if (dailyPercent<50){
                        dailyRatioSpending.setText(dailyPercent + " % used of " + dailyTotalSpentAmountRatio + ". Status: ");
                        dailyRatioSpendingIV.setImageResource(R.drawable.green);
                    } else if (dailyPercent >= 50 && dailyPercent < 100){
                        dailyRatioSpending.setText(dailyPercent + " % used of " + dailyTotalSpentAmountRatio + ". Status: ");
                        dailyRatioSpendingIV.setImageResource(R.drawable.brown);
                    } else {
                        dailyRatioSpending.setText(dailyPercent + " % used of " + dailyTotalSpentAmountRatio + ". Status: ");
                        dailyRatioSpendingIV.setImageResource(R.drawable.red);
                    }

                    float transPercent = (transTotal/transRatio) * 100;
                    if (transPercent<50){
                        progress_ratio_transport.setText(transPercent + " % used of " + transRatio + ". Status: ");
                        transport_status.setImageResource(R.drawable.green);
                    } else if (transPercent >= 50 && transPercent < 100){
                        progress_ratio_transport.setText(transPercent + " % used of " + transRatio + ". Status: ");
                        transport_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_transport.setText(transPercent + " % used of " + transRatio + ". Status: ");
                        transport_status.setImageResource(R.drawable.red);
                    }

                    float foodPercent = (foodTotal/foodRatio) * 100;
                    if (foodPercent<50){
                        progress_ratio_food.setText(foodPercent + " % used of " + foodRatio + ". Status: ");
                        food_status.setImageResource(R.drawable.green);
                    } else if (foodPercent >= 50 && foodPercent < 100){
                        progress_ratio_food.setText(foodPercent + " % used of " + foodRatio + ". Status: ");
                        food_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_food.setText(foodPercent + " % used of " + foodRatio + ". Status: ");
                        food_status.setImageResource(R.drawable.red);
                    }

                    float housePercent = (houseTotal/houseRatio) * 100;
                    if (housePercent<50){
                        progress_ratio_house.setText(housePercent + " % used of " + houseRatio + ". Status: ");
                        house_status.setImageResource(R.drawable.green);
                    } else if (housePercent >= 50 && housePercent < 100){
                        progress_ratio_house.setText(housePercent + " % used of " + houseRatio + ". Status: ");
                        house_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_house.setText(housePercent + " % used of " + houseRatio + ". Status: ");
                        house_status.setImageResource(R.drawable.red);
                    }

                    float entPercent = (entTotal/entRatio) * 100;
                    if (entPercent<50){
                        progress_ratio_ent.setText(entPercent + " % used of " + entRatio + ". Status: ");
                        ent_status.setImageResource(R.drawable.green);
                    } else if (entPercent >= 50 && entPercent < 100){
                        progress_ratio_ent.setText(entPercent + " % used of " + entRatio + ". Status: ");
                        ent_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_ent.setText(entPercent + " % used of " + entRatio + ". Status: ");
                        ent_status.setImageResource(R.drawable.red);
                    }

                    float eduPercent = (eduTotal/eduRatio) * 100;
                    if (eduPercent<50){
                        progress_ratio_edu.setText(eduPercent + " % used of " + eduRatio + ". Status: ");
                        edu_status.setImageResource(R.drawable.green);
                    } else if (eduPercent >= 50 && eduPercent < 100){
                        progress_ratio_edu.setText(eduPercent + " % used of " + eduRatio + ". Status: ");
                        edu_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_edu.setText(eduPercent + " % used of " + eduRatio + ". Status: ");
                        edu_status.setImageResource(R.drawable.red);
                    }

                    float charPercent = (charityTotal/charityRatio) * 100;
                    if (charPercent<50){
                        progress_ratio_charity.setText(charPercent + " % used of " + charityRatio + ". Status: ");
                        charity_status.setImageResource(R.drawable.green);
                    } else if (charPercent >= 50 && charPercent < 100){
                        progress_ratio_charity.setText(charPercent + " % used of " + charityRatio + ". Status: ");
                        charity_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_charity.setText(charPercent + " % used of " + charityRatio + ". Status: ");
                        charity_status.setImageResource(R.drawable.red);
                    }

                    float appPercent = (apparelTotal/appRatio) * 100;
                    if (appPercent<50){
                        progress_ratio_apparel.setText(appPercent + " % used of " + appRatio + ". Status: ");
                        apparel_status.setImageResource(R.drawable.green);
                    } else if (appPercent >= 50 && appPercent < 100){
                        progress_ratio_apparel.setText(appPercent + " % used of " + appRatio + ". Status: ");
                        apparel_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_apparel.setText(appPercent + " % used of " + appRatio + ". Status: ");
                        apparel_status.setImageResource(R.drawable.red);
                    }

                    float healthPercent = (healthTotal/healthRatio) * 100;
                    if (healthPercent<50){
                        progress_ratio_health.setText(healthPercent + " % used of " + healthRatio + ". Status: ");
                        health_status.setImageResource(R.drawable.green);
                    } else if (healthPercent >= 50 && healthPercent < 100){
                        progress_ratio_health.setText(healthPercent + " % used of " + healthRatio + ". Status: ");
                        health_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_health.setText(healthPercent + " % used of " + healthRatio + ". Status: ");
                        health_status.setImageResource(R.drawable.red);
                    }

                    float perPercent = (personalTotal/perRatio) * 100;
                    if (perPercent<50){
                        progress_ratio_personal.setText(perPercent + " % used of " + perRatio + ". Status: ");
                        personal_status.setImageResource(R.drawable.green);
                    } else if (perPercent >= 50 && perPercent < 100){
                        progress_ratio_personal.setText(perPercent + " % used of " + perRatio + ". Status: ");
                        personal_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_personal.setText(perPercent + " % used of " + perRatio + ". Status: ");
                        personal_status.setImageResource(R.drawable.red);
                    }

                    float otherPercent = (otherTotal/otherRatio) * 100;
                    if (otherPercent<50){
                        progress_ratio_other.setText(otherPercent + " % used of " + otherRatio + ". Status: ");
                        other_status.setImageResource(R.drawable.green);
                    } else if (perPercent >= 50 && perPercent < 100){
                        progress_ratio_other.setText(otherPercent + " % used of " + otherRatio + ". Status: ");
                        other_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_ratio_other.setText(otherPercent + " % used of " + otherRatio + ". Status: ");
                        other_status.setImageResource(R.drawable.red);
                    }
                } else {
                    Toast.makeText(DailyAnalyticsActivity.this, "setStatusAndImageResource Errors", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
