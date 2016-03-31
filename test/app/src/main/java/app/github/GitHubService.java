package app.github;

import retrofit2.Call;
import retrofit2.http.GET;
import rx.Observable;

import java.util.List;

public interface GitHubService {

    @GET("users")
    Observable<List<User>> listUsers();

}
