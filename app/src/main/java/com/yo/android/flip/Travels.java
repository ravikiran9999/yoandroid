package com.yo.android.flip;

import java.util.ArrayList;
import java.util.List;

public class Travels {

    private Travels() {

    }

    public static List<Data> getImgDescriptions() {
        return IMG_DESCRIPTIONS;
    }

    private static final List<Data> IMG_DESCRIPTIONS = new ArrayList<Data>();

    static {
        Travels.IMG_DESCRIPTIONS.add(new Data("Top Stories", "Potala Palace", "potala_palace.jpg",
                "The <b>Potala Palace</b> is located in Lhasa, Tibet Autonomous Region, China. It is named after Mount Potalaka, the mythical abode of Chenresig or Avalokitesvara.",
                "China", "Lhasa2", "http://en.wikipedia.org/wiki/Potala_Palace", false));
        Travels.IMG_DESCRIPTIONS.add(new Data("Top Stories", "Drepung Monastery", "drepung_monastery.jpg",
                "<b>Drepung Monastery</b>, located at the foot of Mount Gephel, is one of the \"great three\" Gelukpa university monasteries of Tibet.",
                "China", "Lhasa",
                "http://en.wikipedia.org/wiki/Drepung", false));
        Travels.IMG_DESCRIPTIONS.add(new Data("Entrepreneurship", "Sera Monastery", "sera_monastery.jpg",
                "<b>Sera Monastery</b> is one of the 'great three' Gelukpa university monasteries of Tibet, located 1.25 miles (2.01 km) north of Lhasa.",
                "China", "Lhasa1", "http://en.wikipedia.org/wiki/Sera_Monastery", false));
        Travels.IMG_DESCRIPTIONS.add(new Data("Entrepreneurship","Samye Monastery", "samye_monastery.jpg",
                "<b>Samye Monastery</b> is the first Buddhist monastery built in Tibet, was most probably first constructed between 775 and 779 CE.",
                "China", "Samye",
                "http://en.wikipedia.org/wiki/Samye", false));
    }

    public static final class Data {
        private final String topicName;
        private final String title;
        private final String imageFilename;
        private final String description;
        private final String country;
        private final String city;
        private final String link;
        private boolean isChecked;

        private Data(String topicName, String title, String imageFilename, String description, String country,
                     String city, String link, boolean isChecked) {
            this.title = title;
            this.imageFilename = imageFilename;
            this.description = description;
            this.country = country;
            this.city = city;
            this.link = link;
            this.topicName = topicName;
            this.isChecked = isChecked;
        }

        public String getTopicName() {
            return topicName;
        }

        public String getTitle() {
            return title;
        }

        public String getImageFilename() {
            return imageFilename;
        }

        public String getDescription() {
            return description;
        }

        public String getCountry() {
            return country;
        }

        public String getCity() {
            return city;
        }

        public String getLink() {
            return link;
        }

        public boolean isChecked() {
            return isChecked;
        }
        public void setChecked(boolean isChecked) {
            this.isChecked = isChecked;
        }
    }
}
