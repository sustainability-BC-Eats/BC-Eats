package com.example.bc_eats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main_Activity_TAG";

    private Context mContext;
    private Toolbar toolbar;

    //firebase database
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private Query query;
    private List<Food> foods;

    //Recyclerview
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter adapter;

    private void initializeToolBar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_logout);
        toolbar.setTitle("BC Eats");
        toolbar.setSubtitle("What's free on campus today?");
    }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {

                case R.id.action_post:

                    Intent intent = new Intent(MainActivity.this,CreatePostActivity.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
            return super.onOptionsItemSelected(item);
        }

    private void initializeRecyclerView(){
        recyclerView = (RecyclerView)findViewById(R.id.food_recycler_view);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        initializeToolBar();
        initializeRecyclerView();
        fetch();
    }



    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView mTitle;
        TextView mBuilding;
        TextView mRoom;
        TextView mComment;
        ImageView mImageView;
        String mKey;

        public ViewHolder(View itemView){
            super(itemView);

            mTitle = (TextView) itemView.findViewById(R.id.food_item_title);
            mBuilding = (TextView) itemView.findViewById(R.id.food_item_building);
            mRoom = (TextView) itemView.findViewById(R.id.food_item_room);
            mComment = (TextView) itemView.findViewById(R.id.food_item_comment);
            mImageView = (ImageView)itemView.findViewById(R.id.food_item_img);
        }

        public void setTxtTitle(String string){
            mTitle.setText(string);
        }
        public void setTxtBuilding(String string){
            mBuilding.setText(string);
        }
        public void setTxtRoom(String string){
            mRoom.setText(string);
        }
        public void setTxtComment(String string){
            mComment.setText(string);
        }
        public void setKey(String string){
            mKey = string;
        }
    }

    private void fetch(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM dd, yyyy", Locale.getDefault());
        String todaysDate = dateFormat.format(new Date());
        query = FirebaseDatabase.getInstance().getReference("listings")
                .orderByChild("date")
                .equalTo(todaysDate);

        FirebaseRecyclerOptions<Food> options =
                new FirebaseRecyclerOptions.Builder<Food>()
                        .setQuery(query, Food.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Food, ViewHolder>(options) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food, parent, false);

                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(ViewHolder holder, int position, Food model) {
                holder.setTxtTitle(model.getNotificationTitle());
                holder.setTxtBuilding(model.getBuilding());
                holder.setTxtRoom(model.getRoom());
                holder.setTxtComment(model.getNotificationBody());
                holder.setKey(model.getKey());

                getImg(holder,position,model);
            }

            private void getImg(ViewHolder holder, int position, Food model){
                String fileName = model.getKey() + ".jpg";
                StorageReference imgRef = FirebaseStorage.getInstance().getReference().child("images/" + fileName);

                GlideApp
                        .with(mContext)
                        .load(imgRef)
                        .into(holder.mImageView);
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onResume(){
        super.onResume();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
