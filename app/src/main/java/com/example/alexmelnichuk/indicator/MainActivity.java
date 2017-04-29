package com.example.alexmelnichuk.indicator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.alexmelnichuk.indicator.indicator.IndicatorState;
import com.example.alexmelnichuk.indicator.indicator.IndicatorView;
import com.example.alexmelnichuk.indicator.indicator.SnakeIndicatorView;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    SnakeIndicatorView slimyIndicatorView;
    IndicatorView indicatorView;
    int itemWidth;
    int offsetX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        indicatorView = (IndicatorView) findViewById(R.id.indicator);
        slimyIndicatorView = (SnakeIndicatorView) findViewById(R.id.slimy_indicator);

        itemWidth = IndicatorView.dpToPx(this, 108);

        initRecycler();
        indicatorView.setIndicatorState(new IndicatorState() {
            @Override
            public int numItems() {
                return adapter.getItemCount();
            }
        });
        slimyIndicatorView.setIndicatorState(new IndicatorState() {
            @Override
            public int numItems() {
                return adapter.getItemCount();
            }
        });
    }


    private void initRecycler() {
        recyclerView.setAdapter(adapter = new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
                return new RecyclerView.ViewHolder(v) {};
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ((TextView)holder.itemView.findViewById(R.id.text)).setText(""+(position + 1));
            }

            @Override
            public int getItemCount() {
                return 6;
            }
        });
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                float nonScrolled = (float) recyclerView.getWidth() / itemWidth;
                int padding = (int) (Math.max(0, nonScrolled - 1) * itemWidth);
                recyclerView.setPadding(
                        recyclerView.getPaddingLeft(),
                        recyclerView.getPaddingTop(),
                        padding,
                        recyclerView.getPaddingBottom());
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    Log.d("__SCROLL", "idle");
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                offsetX += dx;
                float nonScrolled = (float) recyclerView.getWidth() / itemWidth;
                //float w = adapter.getItemCount() * itemWidth; //- nonScrolled * itemWidth;
                float pos = (float) offsetX / itemWidth + 0.25f;
                float progress = pos / adapter.getItemCount();
                indicatorView.animate((int) pos);
                slimyIndicatorView.animate((int) pos);
                Log.d("__SCROLL", "progress:" + progress + " pos:" + pos + " nonscrolled:" + nonScrolled);
            }
        });
    }

}
