package com.k2archer.lib.k2router.api;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

import java.io.Serializable;

public class Postcard {
    public static final int FLAG_DEFAULT = -1;

    private Uri mUri;
    private Bundle mBundle;
    private int flag = FLAG_DEFAULT;
    private Activity context;

    private Postcard() {

    }

    public Postcard(Activity context, String path) {
        setContext(context);
        setUri(Uri.parse(path));
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri mUri) {
        this.mUri = mUri;
    }

    public void setUri(String path) {
        this.mUri = Uri.parse(path);
    }

    public Bundle getBundle() {
        return mBundle;
    }

    public void setBundle(Bundle mBundle) {
        this.mBundle = mBundle;
    }

    public Activity getContext() {
        return context;
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    public static class Builder {
        private Postcard meta;

        private Builder() {
            meta = new Postcard();
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder setUri(Uri uri) {
            meta.mUri = uri;
            return this;
        }

        public Builder setUri(String path) {
            meta.mUri = Uri.parse(path);
            return this;
        }

        public Builder setFlag(int flag) {
            meta.flag = flag;
            return this;
        }

        public Builder setBundle(Bundle mBundle) {
            meta.mBundle = mBundle;
            return this;
        }

        public Builder setContext(Activity context) {
            meta.context = context;
            return this;
        }

        public Builder putInt(String key, int value) {
            if (meta.getBundle() == null) {
                meta.setBundle(new Bundle());
            }
            meta.getBundle().putInt(key, value);
            return this;
        }

        public Builder putString(String key, String value) {
            if (meta.getBundle() == null) {
                meta.setBundle(new Bundle());
            }
            meta.getBundle().putString(key, value);
            return this;
        }

        public Builder putSerializable(String key, Serializable value) {
            if (meta.getBundle() == null) {
                meta.setBundle(new Bundle());
            }
            meta.getBundle().putSerializable(key, value);
            return this;
        }

        public Builder putParcelable(String key, Parcelable value) {
            if (meta.getBundle() == null) {
                meta.setBundle(new Bundle());
            }
            meta.getBundle().putParcelable(key, value);
            return this;
        }

        public Postcard build() {
            return meta;
        }

    }
}
