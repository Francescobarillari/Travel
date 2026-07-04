package it.unical.ea.enums;

public enum TravelTag {
    CULTURA("#E0F2FE", "#0369A1"),       // Light Blue, Dark Blue
    STORIA("#FEF3C7", "#B45309"),        // Light Amber, Dark Amber
    CIBO("#FEE2E2", "#B91C1C"),          // Light Red, Dark Red
    AVVENTURA("#DCFCE7", "#15803D"),     // Light Green, Dark Green
    MONTAGNA("#F3F4F6", "#374151"),      // Light Gray, Dark Gray
    NATURA("#D1FAE5", "#047857"),        // Light Emerald, Dark Emerald
    TREKKING("#E0E7FF", "#4338CA"),      // Light Indigo, Dark Indigo
    RELAX("#F3E8FF", "#6B21A8"),         // Light Purple, Dark Purple
    ROMANTICISMO("#FCE7F3", "#BE185D"),  // Light Pink, Dark Pink
    CITTA("#F5F3FF", "#6D28D9"),         // Light Violet, Dark Violet
    MARE("#E0F7FA", "#006064"),          // Light Cyan, Dark Cyan
    SAFARI("#FFF3E0", "#E65100"),        // Light Orange, Dark Orange
    ANIMALI("#F5F5DC", "#5D4037"),       // Light Beige, Dark Brown
    ARTE("#E8F5E9", "#1B5E20"),          // Light Green, Dark Forest Green
    SPORT("#E3F2FD", "#0D47A1"),         // Light Sky Blue, Dark Navy
    FOTOGRAFIA("#ECEFF1", "#37474F"),    // Light Blue-Gray, Dark Slate
    DIVERTIMENTO("#FFFDE7", "#F57F17"),  // Light Yellow, Dark Yellow
    SHOPPING("#FCE4EC", "#880E4F"),      // Light Magenta, Dark Magenta
    SPIAGGIA("#FFF9C4", "#D84315"),      // Light Yellow, Dark Orange
    NEVE("#E1F5FE", "#01579B"),          // Ice Blue, Dark Blue
    AVVENTURA_ESTREMA("#FFEBEE", "#C62828"), // Light Red, Deep Red
    ENOGASTRONOMIA("#EFEBE9", "#4E342E"), // Light Brown, Dark Brown
    MUSEI("#F3E5F5", "#4A148C"),         // Light Violet, Deep Purple
    STORIA_ANTICA("#FFF8E1", "#FF6F00"), // Light Amber, Dark Orange
    ARCHITETTURA("#E0F2FE", "#075985"),  // Light Sky Blue, Dark Ocean Blue
    MUSICA("#FDF2F8", "#9D174D"),        // Soft Pink, Dark Rose
    TEATRO("#FFF1F2", "#9F1239");        // Soft Rose, Dark Crimson

    private final String bgColorHex;
    private final String textColorHex;

    TravelTag(String bgColorHex, String textColorHex) {
        this.bgColorHex = bgColorHex;
        this.textColorHex = textColorHex;
    }

    public String getBgColorHex() {
        return bgColorHex;
    }

    public String getTextColorHex() {
        return textColorHex;
    }
}
