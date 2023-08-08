package cn.wildfire.chat.kit.litapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.wildfire.chat.kit.R;
import cn.wildfirechat.model.LitappInfo;

public class LitappHorListAdapter  extends RecyclerView.Adapter<LitappHorViewHolder> {
    private List<LitappInfo> litappInfos;
    private Fragment fragment;
    private LitappListAdapter.OnLitappClickListener onLitappClickListener;
    private LitappListAdapter.OnLitappLongCLickListener onLitappLongCLickListener;

    public LitappHorListAdapter(Fragment fragment) {
        this.fragment = fragment;
    }

    public void setLitappInfos(List<LitappInfo> litappInfos) {
        this.litappInfos = litappInfos;
    }

    public List<LitappInfo> getLitappInfos() {
        return litappInfos;
    }

    public void setOnLitappItemClickListener(LitappListAdapter.OnLitappClickListener onLitappItemClickListener) {
        this.onLitappClickListener = onLitappItemClickListener;
    }

    public void setOnLitappItemLongCLickListener(LitappListAdapter.OnLitappLongCLickListener onLitappLongCLickListener) {
        this.onLitappLongCLickListener = onLitappLongCLickListener;
    }

    @NonNull
    @Override
    public LitappHorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.litapp_hor_list_item, parent, false);
        LitappHorViewHolder viewHolder = new LitappHorViewHolder(fragment,this,view);
        view.findViewById(R.id.imageRound).setOnClickListener(v -> {
            if (onLitappClickListener != null) {
                onLitappClickListener.onLitappClick(viewHolder.getLitappInfo());
            }
        });
        view.findViewById(R.id.imageRound).setOnLongClickListener(view1 ->{
            if (onLitappLongCLickListener != null){
                onLitappLongCLickListener.onLitappLongClick(viewHolder.getLitappInfo());
            }
            return false;
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LitappHorViewHolder holder, int position) {
        holder.onBind(litappInfos.get(position));
    }

    @Override
    public int getItemCount() {
        return litappInfos == null ? 0 : litappInfos.size();
    }
}
