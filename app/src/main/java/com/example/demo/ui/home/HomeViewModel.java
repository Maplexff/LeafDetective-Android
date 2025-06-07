package com.example.demo.ui.home;

import android.widget.EditText;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

    private static MutableLiveData<String> hint = new MutableLiveData<>();

    public static LiveData<String> getHint() {
        return hint;
    }

    public static void setHint(String hintText) {
        hint.setValue(hintText);
    }
}