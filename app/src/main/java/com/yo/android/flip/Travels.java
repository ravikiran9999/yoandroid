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
                "China", "Lhasa2", "http://en.wikipedia.org/wiki/Potala_Palace"));
        Travels.IMG_DESCRIPTIONS.add(new Data("Top Stories", "Drepung Monastery", "drepung_monastery.jpg",
                "<b>Drepung Monastery</b>, located at the foot of Mount Gephel, is one of the \"great three\" Gelukpa university monasteries of Tibet.",
                "China", "Lhasa",
                "http://en.wikipedia.org/wiki/Drepung"));
        Travels.IMG_DESCRIPTIONS.add(new Data("Entrepreneurship", "Sera Monastery", "sera_monastery.jpg",
                "<b>Sera Monastery</b> is one of the 'great three' Gelukpa university monasteries of Tibet, located 1.25 miles (2.01 km) north of Lhasa.",
                "China", "Lhasa1", "http://en.wikipedia.org/wiki/Sera_Monastery"));
        Travels.IMG_DESCRIPTIONS.add(new Data("Entrepreneurship","Samye Monastery", "samye_monastery.jpg",
                "<b>Samye Monastery</b> is the first Buddhist monastery built in Tibet, was most probably first constructed between 775 and 779 CE.",
                "China", "Samye",
                "http://en.wikipedia.org/wiki/Samye"));
        /*Travels.IMG_DESCRIPTIONS.add(
                new Data("Tashilunpo Monastery", "tashilunpo_monastery.jpg",
                        "<b>Tashilhunpo Monastery</b>, founded in 1447 by Gendun Drup, the First Dalai Lama, is a historic and culturally important monastery next to Shigatse, the second-largest city in Tibet.",
                        "China", "Shigatse",
                        "http://en.wikipedia.org/wiki/Tashilhunpo_Monastery"));
        Travels.IMG_DESCRIPTIONS.add(new Data("Zhangmu Port", "zhangmu_port.jpg",
                "<b>Zhangmu/Dram</b> is a customs town and port of entry located in Nyalam County on the Nepal-China border, just uphill and across the Bhote Koshi River from the Nepalese town of Kodari.",
                "China", "Zhangmu",
                "http://en.wikipedia.org/wiki/Zhangmu"));
        Travels.IMG_DESCRIPTIONS.add(new Data("Kathmandu", "kathmandu.jpg",
                "<b>Kathmandu</b> is the capital and, with more than one million inhabitants, the largest metropolitan city of Nepal.",
                "Nepal", "Kathmandu",
                "http://en.wikipedia.org/wiki/Kathmandu"));
        Travels.IMG_DESCRIPTIONS.add(new Data("Pokhara", "pokhara.jpg",
                "<b>Pokhara Sub-Metropolitan City</b> is the second largest city of Nepal with approximately 250,000 inhabitants and is situated about 200 km west of the capital Kathmandu.",
                "Nepal", "Pokhara",
                "http://en.wikipedia.org/wiki/Pokhara"));
        Travels.IMG_DESCRIPTIONS.add(new Data("Patan", "patan.jpg",
                "<b>Patan</b>, officially Lalitpur Sub-Metropolitan City, is one of the major cities of Nepal located in the south-central part of Kathmandu Valley.",
                "Nepal", "Patan",
                "http://en.wikipedia.org/wiki/Patan,_Nepal"));*/
    }

    public static final class Data {
        private final String topicName;
        private final String title;
        private final String imageFilename;
        private final String description;
        private final String country;
        private final String city;
        private final String link;

        private Data(String topicName, String title, String imageFilename, String description, String country,
                     String city, String link) {
            this.title = title;
            this.imageFilename = imageFilename;
            this.description = description;
            this.country = country;
            this.city = city;
            this.link = link;
            this.topicName = topicName;
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

    }
}
