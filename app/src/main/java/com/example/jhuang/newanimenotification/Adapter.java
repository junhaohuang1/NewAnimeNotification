package com.example.jhuang.newanimenotification;

import android.content.Context;
import android.nfc.Tag;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.example.jhuang.newanimenotification.Model;
import com.example.jhuang.newanimenotification.FirebaseActions;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {


    private FirebaseActions firebaseInstance = new FirebaseActions();
    private List<Model> items = new ArrayList<>();
    private Set<String> subscribedTopicsList;
    SparseBooleanArray itemStateArray= new SparseBooleanArray();
    Adapter(Set<String> newSubscribedTopicsList) {
        setSubscribedTopicsList(newSubscribedTopicsList);
    }

    public Set<String> getSubscribedTopicsList(){
        return this.subscribedTopicsList;
    }

    public void setSubscribedTopicsList(Set<String> newSubscribedTopicsList){
        this.subscribedTopicsList = newSubscribedTopicsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutForItem = R.layout.list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutForItem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(position, getSubscribedTopicsList());
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    void loadItems(List<Model> tournaments) {
        this.items = tournaments;
        notifyDataSetChanged();
    }




    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CheckedTextView mCheckedTextView;

        ViewHolder(View itemView) {
            super(itemView);
            mCheckedTextView = (CheckedTextView) itemView.findViewById(R.id.checked_text_view);
//            System.out.println("creating viewholder");
            itemView.setOnClickListener(this);

        }

        void bind(int position, Set<String> subscribedTopicsList) {
            // use the sparse boolean array to check
            if (!itemStateArray.get(position, false)) {
                mCheckedTextView.setChecked(false);}
            else {
                mCheckedTextView.setChecked(true);
            }
            String currentBoxText = String.valueOf(items.get(position).getAnimeName());
            mCheckedTextView.setText(currentBoxText);
            checkIfAnimeIsSubscribed(subscribedTopicsList, position, currentBoxText);
        }

        void checkIfAnimeIsSubscribed(Set<String> subscribedTopicsList, int position, String currentBoxText){
            if(!items.get(position).getIsExaminedInSubscriptions()){
                if(subscribedTopicsList.contains(currentBoxText.replaceAll("\\s+",""))){
                    mCheckedTextView.setChecked(true);
                    itemStateArray.put(position, true);
                }
                items.get(position).setIsExaminedInSubscriptions(true);
            }
        }


        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            if (!itemStateArray.get(adapterPosition, false)) {
                mCheckedTextView.setChecked(true);
                itemStateArray.put(adapterPosition, true);
                firebaseInstance.subscribeToTopics(String.valueOf(items.get(adapterPosition).getAnimeName()));
            }
            else  {
                mCheckedTextView.setChecked(false);
                itemStateArray.put(adapterPosition, false);
                firebaseInstance.unsubscribeToTopics(String.valueOf(items.get(adapterPosition).getAnimeName()));
            }
        }



    }
}