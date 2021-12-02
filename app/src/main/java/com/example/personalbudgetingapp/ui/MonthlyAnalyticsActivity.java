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
import org.joda.time.Months;
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MonthlyAnalyticsActivity extends AppCompatActivity {

    private Toolbar monthToolbar;

    private FirebaseAuth firebaseAuth;
    private String onlineUserId = "";
    private DatabaseReference expensesRef, personalRef;
    private AnyChartView anyChartView;

    private TextView totalBudgetAmountTextView, analyticsTransportAmount, analyticsFoodAmount, analyticsHouseAmount,
            analyticsEntAmount, analyticsEduAmount, analyticsApparelAmount, analyticsCharityAmount, analyticsHealthAmount,
            analyticsPersonalAmount, analyticsOtherAmount, monthSpentAmount;

    private RelativeLayout transportRL, foodRL, houseRL, entRL, eduRL, apparelRL, charityRL, healthRL, personalRL, otherRl;

    private TextView progress_ratio_transport, progress_ratio_food, progress_ratio_house, progress_ratio_ent, progress_ratio_edu,
            progress_ratio_apparel, progress_ratio_charity, progress_ratio_health, progress_ratio_personal, progress_ratio_other,
            monthRatioSpending;

    private ImageView transport_status, food_status, house_status, ent_status, edu_status, apparel_status, charity_status,
            health_status, personal_status, other_status, monthRatioSpendingIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_analytics);

        monthToolbar = findViewById(R.id.monthToolbar);
        setSupportActionBar(monthToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Monthly Analytics");

        firebaseAuth = FirebaseAuth.getInstance();
        onlineUserId = firebaseAuth.getCurrentUser().getUid();
        expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        personalRef = FirebaseDatabase.getInstance().getReference("personal").child(onlineUserId);

        totalBudgetAmountTextView = findViewById(R.id.totalAmountSpentOn);
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
        monthSpentAmount = findViewById(R.id.monthSpentAmount);

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
        monthRatioSpending = findViewById(R.id.monthRatioSpending);

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
        monthRatioSpendingIV = findViewById(R.id.monthRatioSpendingIV);

        anyChartView = findViewById(R.id.anyChartView);

        getTotalMonthTransportExpense();
        getTotalMonthFoodExpense();
        getTotalMonthHouseExpense();
        getTotalMonthEducationExpense();
        getTotalMonthEntertainmentExpense();
        getTotalMonthCharityExpense();
        getTotalMonthApparelExpense();
        getTotalMonthHealthExpense();
        getTotalMonthPersonalExpense();
        getTotalMonthOtherExpense();
        getTotalMonthSpending();

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

    private void getTotalMonthTransportExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNMonth = "Transport"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNMonth").equalTo(itemNMonth);
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
                    personalRef.child("monthTrans").setValue(totalAmount);
                } else {
                    transportRL.setVisibility(View.GONE);
                    personalRef.child("monthTrans").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalMonthFoodExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNMonth = "Food"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNMonth").equalTo(itemNMonth);
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
                    personalRef.child("monthFood").setValue(totalAmount);
                } else {
                    foodRL.setVisibility(View.GONE);
                    personalRef.child("monthFood").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalMonthHouseExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNMonth = "House"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNMonth").equalTo(itemNMonth);
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
                    personalRef.child("monthHouse").setValue(totalAmount);
                } else {
                    houseRL.setVisibility(View.GONE);
                    personalRef.child("monthHouse").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalMonthEducationExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNMonth = "Education"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNMonth").equalTo(itemNMonth);
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
                    personalRef.child("monthEdu").setValue(totalAmount);
                } else {
                    eduRL.setVisibility(View.GONE);
                    personalRef.child("monthEdu").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalMonthEntertainmentExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNMonth = "Entertainment"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNMonth").equalTo(itemNMonth);
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
                    personalRef.child("monthEnt").setValue(totalAmount);
                } else {
                    entRL.setVisibility(View.GONE);
                    personalRef.child("monthEnt").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalMonthCharityExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNMonth = "Charity"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNMonth").equalTo(itemNMonth);
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
                    personalRef.child("monthCharity").setValue(totalAmount);
                } else {
                    charityRL.setVisibility(View.GONE);
                    personalRef.child("monthCharity").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalMonthApparelExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNMonth = "Apparel"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNMonth").equalTo(itemNMonth);
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
                    personalRef.child("monthApparel").setValue(totalAmount);
                } else {
                    apparelRL.setVisibility(View.GONE);
                    personalRef.child("monthApparel").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalMonthHealthExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNMonth = "Health"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNMonth").equalTo(itemNMonth);
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
                    personalRef.child("monthHealth").setValue(totalAmount);
                } else {
                    healthRL.setVisibility(View.GONE);
                    personalRef.child("monthHealth").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalMonthPersonalExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNMonth = "Personal"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNMonth").equalTo(itemNMonth);
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
                    personalRef.child("monthPersonal").setValue(totalAmount);
                } else {
                    personalRL.setVisibility(View.GONE);
                    personalRef.child("monthPersonal").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalMonthOtherExpense(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        String itemNMonth = "Other"+months.getMonths();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNMonth").equalTo(itemNMonth);
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
                    personalRef.child("monthOther").setValue(totalAmount);
                } else {
                    otherRl.setVisibility(View.GONE);
                    personalRef.child("monthOther").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalMonthSpending(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("month").equalTo(months.getMonths());
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
                    }
                    totalBudgetAmountTextView.setText("Total month's spending: Rs " + totalAmount);
                    monthSpentAmount.setText("Total spent: Rs " + totalAmount);
                } else {
                    anyChartView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGraph(){
        personalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int transTotal;
                    if (snapshot.hasChild("monthTrans")){
                        transTotal = Integer.parseInt(snapshot.child("monthTrans").getValue().toString());
                    } else {
                        transTotal = 0;
                    }

                    int foodTotal;
                    if (snapshot.hasChild("monthFood")){
                        foodTotal = Integer.parseInt(snapshot.child("monthFood").getValue().toString());
                    } else {
                        foodTotal = 0;
                    }

                    int houseTotal;
                    if (snapshot.hasChild("monthHouse")){
                        houseTotal = Integer.parseInt(snapshot.child("monthHouse").getValue().toString());
                    } else {
                        houseTotal = 0;
                    }

                    int entTotal;
                    if (snapshot.hasChild("monthEnt")){
                        entTotal = Integer.parseInt(snapshot.child("monthEnt").getValue().toString());
                    } else {
                        entTotal = 0;
                    }

                    int eduTotal;
                    if (snapshot.hasChild("monthEdu")){
                        eduTotal = Integer.parseInt(snapshot.child("monthEdu").getValue().toString());
                    } else {
                        eduTotal = 0;
                    }

                    int charityTotal;
                    if (snapshot.hasChild("monthCharity")){
                        charityTotal = Integer.parseInt(snapshot.child("monthCharity").getValue().toString());
                    } else {
                        charityTotal = 0;
                    }

                    int apparelTotal;
                    if (snapshot.hasChild("monthApparel")){
                        apparelTotal = Integer.parseInt(snapshot.child("monthApparel").getValue().toString());
                    } else {
                        apparelTotal = 0;
                    }

                    int healthTotal;
                    if (snapshot.hasChild("monthHealth")){
                        healthTotal = Integer.parseInt(snapshot.child("monthHealth").getValue().toString());
                    } else {
                        healthTotal = 0;
                    }

                    int personalTotal;
                    if (snapshot.hasChild("monthPersonal")){
                        personalTotal = Integer.parseInt(snapshot.child("monthPersonal").getValue().toString());
                    } else {
                        personalTotal = 0;
                    }

                    int otherTotal;
                    if (snapshot.hasChild("monthOther")){
                        otherTotal = Integer.parseInt(snapshot.child("monthOther").getValue().toString());
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
                    pie.title("Monthly Analytics");
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
                    Toast.makeText(MonthlyAnalyticsActivity.this, "Child does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyAnalyticsActivity.this, "Child does not exist", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setStatusAndImageResource(){
        personalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    float transTotal;
                    if (snapshot.hasChild("monthTrans")){
                        transTotal = Integer.parseInt(snapshot.child("monthTrans").getValue().toString());
                    } else {
                        transTotal = 0;
                    }

                    float foodTotal;
                    if (snapshot.hasChild("monthFood")){
                        foodTotal = Integer.parseInt(snapshot.child("monthFood").getValue().toString());
                    } else {
                        foodTotal = 0;
                    }

                    float houseTotal;
                    if (snapshot.hasChild("monthHouse")){
                        houseTotal = Integer.parseInt(snapshot.child("monthHouse").getValue().toString());
                    } else {
                        houseTotal = 0;
                    }

                    float entTotal;
                    if (snapshot.hasChild("monthEnt")){
                        entTotal = Integer.parseInt(snapshot.child("monthEnt").getValue().toString());
                    } else {
                        entTotal = 0;
                    }

                    float eduTotal;
                    if (snapshot.hasChild("monthEdu")){
                        eduTotal = Integer.parseInt(snapshot.child("monthEdu").getValue().toString());
                    } else {
                        eduTotal = 0;
                    }

                    float charityTotal;
                    if (snapshot.hasChild("monthCharity")){
                        charityTotal = Integer.parseInt(snapshot.child("monthCharity").getValue().toString());
                    } else {
                        charityTotal = 0;
                    }

                    float apparelTotal;
                    if (snapshot.hasChild("monthApparel")){
                        apparelTotal = Integer.parseInt(snapshot.child("monthApparel").getValue().toString());
                    } else {
                        apparelTotal = 0;
                    }

                    float healthTotal;
                    if (snapshot.hasChild("monthHealth")){
                        healthTotal = Integer.parseInt(snapshot.child("monthHealth").getValue().toString());
                    } else {
                        healthTotal = 0;
                    }

                    float personalTotal;
                    if (snapshot.hasChild("monthPersonal")){
                        personalTotal = Integer.parseInt(snapshot.child("monthPersonal").getValue().toString());
                    } else {
                        personalTotal = 0;
                    }

                    float otherTotal;
                    if (snapshot.hasChild("monthOther")){
                        otherTotal = Integer.parseInt(snapshot.child("monthOther").getValue().toString());
                    } else {
                        otherTotal = 0;
                    }

                    float monthTotalSpentAmount;
                    if (snapshot.hasChild("month")){
                        monthTotalSpentAmount = Integer.parseInt(snapshot.child("month").getValue().toString());
                    } else {
                        monthTotalSpentAmount = 0;
                    }


                    //Getting Ratios
                    float transRatio;
                    if (snapshot.hasChild("weekTransRatio")){
                        transRatio = Integer.parseInt(snapshot.child("weekTransRatio").getValue().toString());
                    } else {
                        transRatio = 0;
                    }

                    float foodRatio;
                    if (snapshot.hasChild("weekFoodRatio")){
                        foodRatio = Integer.parseInt(snapshot.child("weekFoodRatio").getValue().toString());
                    } else {
                        foodRatio = 0;
                    }

                    float houseRatio;
                    if (snapshot.hasChild("weekHouseRatio")){
                        houseRatio = Integer.parseInt(snapshot.child("weekHouseRatio").getValue().toString());
                    } else {
                        houseRatio = 0;
                    }

                    float entRatio;
                    if (snapshot.hasChild("weekEntRatio")){
                        entRatio = Integer.parseInt(snapshot.child("weekEntRatio").getValue().toString());
                    } else {
                        entRatio = 0;
                    }

                    float eduRatio;
                    if (snapshot.hasChild("weekEduRatio")){
                        eduRatio = Integer.parseInt(snapshot.child("weekEduRatio").getValue().toString());
                    } else {
                        eduRatio = 0;
                    }

                    float charityRatio;
                    if (snapshot.hasChild("weekCharRatio")){
                        charityRatio = Integer.parseInt(snapshot.child("weekCharRatio").getValue().toString());
                    } else {
                        charityRatio = 0;
                    }

                    float appRatio;
                    if (snapshot.hasChild("weekAppRatio")){
                        appRatio = Integer.parseInt(snapshot.child("weekAppRatio").getValue().toString());
                    } else {
                        appRatio = 0;
                    }

                    float healthRatio;
                    if (snapshot.hasChild("weekHealthRatio")){
                        healthRatio = Integer.parseInt(snapshot.child("weekHealthRatio").getValue().toString());
                    } else {
                        healthRatio = 0;
                    }

                    float perRatio;
                    if (snapshot.hasChild("weekPerRatio")){
                        perRatio = Integer.parseInt(snapshot.child("weekPerRatio").getValue().toString());
                    } else {
                        perRatio = 0;
                    }

                    float otherRatio;
                    if (snapshot.hasChild("weekOtherRatio")){
                        otherRatio = Integer.parseInt(snapshot.child("weekOtherRatio").getValue().toString());
                    } else {
                        otherRatio = 0;
                    }

                    float monthTotalSpentAmountRatio;
                    if (snapshot.hasChild("budget")){
                        monthTotalSpentAmountRatio = Integer.parseInt(snapshot.child("budget").getValue().toString());
                    } else {
                        monthTotalSpentAmountRatio = 0;
                    }


                    //getting % and setting image
                    float monthPercent = (monthTotalSpentAmount/monthTotalSpentAmountRatio) * 100;
                    if (monthPercent<50){
                        monthRatioSpending.setText(monthPercent + " % used of " + monthTotalSpentAmountRatio + ". Status: ");
                        monthRatioSpendingIV.setImageResource(R.drawable.green);
                    } else if (monthPercent >= 50 && monthPercent < 100){
                        monthRatioSpending.setText(monthPercent + " % used of " + monthTotalSpentAmountRatio + ". Status: ");
                        monthRatioSpendingIV.setImageResource(R.drawable.brown);
                    } else {
                        monthRatioSpending.setText(monthPercent + " % used of " + monthTotalSpentAmountRatio + ". Status: ");
                        monthRatioSpendingIV.setImageResource(R.drawable.red);
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
                    Toast.makeText(MonthlyAnalyticsActivity.this, "setStatusAndImageResource Errors", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}