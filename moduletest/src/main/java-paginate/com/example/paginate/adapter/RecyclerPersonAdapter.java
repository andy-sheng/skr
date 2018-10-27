package com.example.paginate.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.common.view.recyclerview.DiffAdapter;
import com.example.paginate.data.Person;
import com.wali.live.moduletest.R;

import java.util.List;

public class RecyclerPersonAdapter extends DiffAdapter implements RecyclerOnItemClickListener {

    public RecyclerPersonAdapter(List<Person> data) {
        mDataList.addAll(data);
    }

    @Override
    public PersonVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.person_list_item, parent, false);
        return new PersonVH(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PersonVH) {
            PersonVH vh = (PersonVH) holder;
            Person person = (Person) mDataList.get(position);
            vh.tvFullName.setText(person.toString());
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public void onItemClicked(View view, int position) {
        Toast.makeText(view.getContext(), "Clicked position: " + position, Toast.LENGTH_SHORT).show();
        if (position >= 0 && position < mDataList.size()) {
            this.mDataList.remove(position);
            notifyItemRemoved(position);
        }
    }
    public static class PersonVH extends RecyclerView.ViewHolder {
        TextView tvFullName;

        public PersonVH(View view, final RecyclerOnItemClickListener recyclerOnItemClickListener) {
            super(view);
            this.tvFullName = (TextView) view.findViewById(R.id.tv_full_name);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recyclerOnItemClickListener != null) {
                        recyclerOnItemClickListener.onItemClicked(v, getAdapterPosition());
                    }
                }
            });
        }
    }

}