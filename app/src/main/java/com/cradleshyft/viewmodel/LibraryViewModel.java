package com.cradleshyft.dslrcamera.viewmodel;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.List;
import com.cradleshyft.dslrcamera.model.MediaItemObj;
import com.cradleshyft.dslrcamera.util.CursorUtil;

public class LibraryViewModel extends AndroidViewModel {
    public LibraryViewModel(@NonNull Application application) {
        super(application);
    }

    private ContentObserver contentObserver = null;

    private final MutableLiveData<MediaItemObj> mediaItemMLD = new MutableLiveData<>();
    public LiveData<MediaItemObj> mediaItem = mediaItemMLD;

    public void loadMediaItems() {
        List<MediaItemObj> list = new ArrayList<>();
        Cursor cursor = getApplication().getContentResolver().query(
                CursorUtil.getCursorUri(),
                CursorUtil.getProjection(),
                CursorUtil.getSelection(),
                null,
                CursorUtil.getSortOrder()
        );

        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
        int mediaTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(idColumn);
            int mediaType = cursor.getInt(mediaTypeColumn);
            Uri uri = ContentUris.withAppendedId(
                    CursorUtil.getMediaContentUri(mediaType), id
            );
            MediaItemObj item = new MediaItemObj(uri, mediaType);
            list.add(item);
        }
        if (!list.isEmpty())
            mediaItemMLD.postValue(list.get(0));
        cursor.close();
        registerMediaObserver();
    }

    private void registerMediaObserver() {
        if (contentObserver == null) {
            ContentObserver observer = new ContentObserver(new Handler(Looper.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    loadMediaItems();
                }
            };
            ContentResolver contentResolver = getApplication().getContentResolver();
            contentResolver.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, observer);
            contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, observer);
            contentObserver = observer;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (contentObserver != null) {
            getApplication().getContentResolver().unregisterContentObserver(contentObserver);
        }
    }

}
