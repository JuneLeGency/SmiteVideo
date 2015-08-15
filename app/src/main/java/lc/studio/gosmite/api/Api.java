package lc.studio.gosmite.api;

import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.Rest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.List;

import lc.studio.gosmite.model.Comment;
import lc.studio.gosmite.model.Commentator;
import lc.studio.gosmite.model.Token;
import lc.studio.gosmite.model.Video;

/**
 * Created by legency on 14-12-22.
 */
@Rest(rootUrl = "http://www.esportsbox.com/api/v1/", converters = {MappingJackson2HttpMessageConverter.class})
public interface Api {

    @Post("account/preregister.json?platform={platform}&client_id={client_id}")
    Token register(String platform, String client_id);

    @Get("games/2/videos.json?page={page}&access_token=5477657b3653cfdbef4bfa7080d588a5ae73a7e55a2fc84e2ddfef03013adb6b")
    List<Video> getVideos(int page);

    @Get("games/2/videos.json?page={page}&q[commentator_id_eq]={commentator_id}&access_token=5477657b3653cfdbef4bfa7080d588a5ae73a7e55a2fc84e2ddfef03013adb6b")
    List<Video> getVideosByCommentator(int page,String commentator_id);

    @Get("games/2/videos/{video}.json?access_token=5477657b3653cfdbef4bfa7080d588a5ae73a7e55a2fc84e2ddfef03013adb6b")
    Video getVideoDetail(String video);

    @Get("games/2/videos/{video}/comments.json?access_token=5477657b3653cfdbef4bfa7080d588a5ae73a7e55a2fc84e2ddfef03013adb6b")
    List<Comment> getVideoComments(String video);

    @Get("games/2/commentators.json?access_token=5477657b3653cfdbef4bfa7080d588a5ae73a7e55a2fc84e2ddfef03013adb6b")
    List<Commentator> getCommentators();

    @Post("games/2/videos/{video}/comments.json?body={body}&access_token=5477657b3653cfdbef4bfa7080d588a5ae73a7e55a2fc84e2ddfef03013adb6b")
    Comment postComment(String video,String body);
}
