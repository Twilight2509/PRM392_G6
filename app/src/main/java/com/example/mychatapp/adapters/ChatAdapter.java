package com.example.mychatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mychatapp.databinding.ItemContainerReceivedMessageBinding;
import com.example.mychatapp.databinding.ItemContainerSentMessageBinding;
import com.example.mychatapp.models.ChatMessages;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessages> chatMessages;
    private Bitmap receiverProfileImage;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(Bitmap bitmap) {
        receiverProfileImage = bitmap;
    }

    public ChatAdapter(List<ChatMessages> chatMessages, Bitmap receiverProfileImage, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SendMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SendMessageViewHolder) holder).setData(chatMessages.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SendMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerSentMessageBinding binding;

        SendMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessages chatMessage) {
            if (chatMessage.image != null && !chatMessage.image.isEmpty()) {
                // Hiển thị hình ảnh nếu trường image không null và không rỗng
                Bitmap imageBitmap = getBitmapFromEncodedString(chatMessage.image);
                if (imageBitmap != null) {
                    binding.imageMessage.setImageBitmap(imageBitmap);
                    binding.imageMessage.setVisibility(View.VISIBLE);
                    binding.textMessage.setVisibility(View.GONE);
                }
            } else if (chatMessage.message != null && !chatMessage.message.isEmpty()) {
                // Hiển thị văn bản nếu trường message không null và không rỗng
                binding.textMessage.setText(chatMessage.message);
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.imageMessage.setVisibility(View.GONE);
            }
            binding.textDateTime.setText(chatMessage.dateTime);
        }

        private Bitmap getBitmapFromEncodedString(String encodedImage) {
            try {
                byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap == null) {
                    Log.e("ChatAdapter", "Failed to decode Bitmap from Base64");
                }
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ChatAdapter", "Error decoding image: " + e.getMessage());
                return null;
            }
        }

    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessages chatMessage, Bitmap receiverProfileImage) {
            if (chatMessage.image != null && !chatMessage.image.isEmpty()) {
                // Hiển thị hình ảnh nếu trường image không null và không rỗng
                Bitmap imageBitmap = getBitmapFromEncodedString(chatMessage.image);
                if (imageBitmap != null) {
                    binding.imageMessage.setImageBitmap(imageBitmap);
                    binding.imageMessage.setVisibility(View.VISIBLE);
                    binding.textMessage.setVisibility(View.GONE);
                }
            } else if (chatMessage.message != null && !chatMessage.message.isEmpty()) {
                // Hiển thị văn bản nếu trường message không null và không rỗng
                binding.textMessage.setText(chatMessage.message);
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.imageMessage.setVisibility(View.GONE);
            }
            binding.textDateTime.setText(chatMessage.dateTime);
            if (receiverProfileImage != null) {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }
        }

        private Bitmap getBitmapFromEncodedString(String encodedImage) {
            try {
                byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
