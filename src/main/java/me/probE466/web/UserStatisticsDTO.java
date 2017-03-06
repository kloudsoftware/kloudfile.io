package me.probE466.web;

public class UserStatisticsDTO {
    private final String userName;
    private final int imageCount;

    public UserStatisticsDTO(String userName, int imageCount) {
        this.userName = userName;
        this.imageCount = imageCount;
    }

    public String getUserName() {
        return userName;
    }

    public int getImageCount() {
        return imageCount;
    }
}
