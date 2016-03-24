package app.github;

import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

public interface GitHubService {

    @GET("users")
    Call<List<User>> listUsers();

}
