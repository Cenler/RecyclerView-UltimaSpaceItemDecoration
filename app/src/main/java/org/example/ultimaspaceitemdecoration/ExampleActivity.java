package org.example.ultimaspaceitemdecoration;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.cenler.ultimaspaceitemdecoration.UltimaSpaceItemDecoration;


public class ExampleActivity extends AppCompatActivity {

    private RecyclerView mListRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar supportActionBar = getSupportActionBar();


        mListRV = findViewById(R.id.rv_list);
        final Context context = mListRV.getContext();
        mListRV.addItemDecoration(new UltimaSpaceItemDecoration.DividerBuilder(context)
                .setColorRes(R.color.purple_200)
                .setDividerSize(10)
                .build());
        mListRV.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        mListRV.setAdapter(new DividerItemAdapter());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final Context context = mListRV.getContext();
        mListRV.removeItemDecorationAt(0);
        int itemId = item.getItemId();
        if (itemId == R.id.action_lv) {
            mListRV.setLayoutManager(new LinearLayoutManager(context));
            mListRV.addItemDecoration(new UltimaSpaceItemDecoration.DividerBuilder(context)
                    .setColorRes(R.color.purple_200)
                    .setDividerSize(10)
                    .setPaddingStart(16)
                    .setPaddingEnd(16)
                    .setPaddingTop(16)
                    .setPaddingBottom(16)
                    .build());
            return true;
        } else if (itemId == R.id.action_lo) {
            mListRV.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            mListRV.addItemDecoration(new UltimaSpaceItemDecoration.DividerBuilder(context)
                    .setColorRes(R.color.purple_200)
                    .setDividerSize(10)
                    .setPaddingStart(16)
                    .setPaddingEnd(16)
                    .setPaddingTop(16)
                    .setPaddingBottom(16)
                    .build());
            return true;
        } else if (itemId == R.id.action_gv) {
            mListRV.setLayoutManager(new GridLayoutManager(context, 3));
            mListRV.addItemDecoration(new UltimaSpaceItemDecoration.DividerBuilder(context)
                    .setPaddingStart(16)
                    .setVerticalSpace(8)
                    .setHorizontalSpace(8)
                    .setPaddingEnd(16)
                    .setPaddingTop(16)
                    .setPaddingBottom(16)
                    .build());
            return true;
        } else if (itemId == R.id.action_go) {
            final GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3, LinearLayoutManager.HORIZONTAL, false);
            mListRV.setLayoutManager(gridLayoutManager);
            mListRV.addItemDecoration(new UltimaSpaceItemDecoration.DividerBuilder(context)
                    .setPaddingStart(16)
                    .setVerticalSpace(8)
                    .setHorizontalSpace(8)
                    .setPaddingEnd(16)
                    .setPaddingTop(16)
                    .setPaddingBottom(16)
                    .build());
            return true;
        } else if (itemId == R.id.action_sv) {
            final StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
            mListRV.setLayoutManager(gridLayoutManager);
            mListRV.addItemDecoration(new UltimaSpaceItemDecoration.DividerBuilder(context)
                    .setPaddingStart(16)
                    .setVerticalSpace(8)
                    .setHorizontalSpace(8)
                    .setPaddingEnd(16)
                    .setPaddingTop(16)
                    .setPaddingBottom(16)
                    .build());
            return true;
        } else if (itemId == R.id.action_so) {
            final StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.HORIZONTAL);
            mListRV.setLayoutManager(gridLayoutManager);
            mListRV.addItemDecoration(new UltimaSpaceItemDecoration.DividerBuilder(context)
                    .setPaddingStart(16)
                    .setVerticalSpace(8)
                    .setHorizontalSpace(8)
                    .setPaddingEnd(16)
                    .setPaddingTop(16)
                    .setPaddingBottom(16)
                    .build());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class DividerItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_example_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            holder.mTextTV.setText(String.format("Item: %s", position));
        }

        @Override
        public int getItemCount() {
            return 30;
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        public final TextView mTextTV;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            mTextTV = itemView.findViewById(R.id.tv_text);
        }
    }
}