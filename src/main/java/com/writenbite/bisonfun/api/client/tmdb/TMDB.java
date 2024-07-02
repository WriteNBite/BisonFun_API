package com.writenbite.bisonfun.api.client.tmdb;

public enum TMDB {

    /**
     * Link to image "No Image"
     */
    NO_IMAGE("https://img.freepik.com/free-vector/flat-design-no-photo-sign-design_23-2149289006.jpg"),
    /**
     * Link to image system from TheMovieDB API.
     * Add "poster path" from API to get image
     */
    IMAGE_500("https://image.tmdb.org/t/p/w500"),
    IMAGE_200("https://image.tmdb.org/t/p/w200"),
    /**
     * Link to get info about movie by id.
     */
    MOVIE("https://api.themoviedb.org/3/movie/{movie_id}"),
    /**
     * Link to get info about tv show by id.
     */
    TV("https://api.themoviedb.org/3/tv/{tv_id}"),
    /**
     * Link to movie search system from TheMovieDB API.
     */
    SEARCH_MOVIE("https://api.themoviedb.org/3/search/movie"),
    /**
     * Link to movie search system from TheMovieDB API.
     */
    SEARCH_TV("https://api.themoviedb.org/3/search/tv"),
    /**
     * Link to get movie trends
     * */
    TRENDS_MOVIE("https://api.themoviedb.org/3/trending/movie/week"),
    /**
     * Link to get tv trends
     */
    TRENDS_TV("https://api.themoviedb.org/3/trending/tv/week");

    final public String link;

    TMDB(String link){
        this.link = link;
    }
}
