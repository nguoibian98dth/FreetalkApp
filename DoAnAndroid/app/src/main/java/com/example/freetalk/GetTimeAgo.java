package com.example.freetalk;

import android.app.Application;
import android.content.Context;

    public class GetTimeAgo extends Application {

        private static final int SECOND_MILLIS = 1000;
        private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


        public static String getTimeAgo(long time, Context context) {
            if (time < 1000000000000L) {
                time *= 1000;
            }

            long now = System.currentTimeMillis();
            if (time > now || time <= 0) {
                return null;
            }

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "vừa mới Online";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "một phút trước";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " phút trước";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "một giờ trước";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " giờ trước";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "hôm qua";
            } else {
                return diff / DAY_MILLIS + " ngày trước";
            }
        }


    }


