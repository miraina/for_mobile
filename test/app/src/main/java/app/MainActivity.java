package app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import app.github.GitHubService;
import app.github.User;
import com.squareup.picasso.Picasso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import retrofit.RxJavaCallAdapterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.internal.util.SubscriptionList;
import rx.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class MainActivity extends AppCompatActivity {

    public static final Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);
    public static final String PREF_NAME = "favourite";
    private SubscriptionList subscriptionList;
    private final List<User> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        RecyclerView rv = (RecyclerView) findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        final RecyclerView.Adapter adapter = new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.new_layout, parent, false);
                return new RecyclerView.ViewHolder(view) {
                };
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                        boolean isLove = preferences.getBoolean(users.get(position).login, false);
                        preferences.edit().putBoolean(users.get(position).login, !isLove).apply();
                        notifyItemChanged(position);
                    }
                });
                SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                boolean isLove = preferences.getBoolean(users.get(position).login, false);

                if (isLove){
                    holder.itemView.setBackgroundColor(getResources().getColor(R.color.colorMyLove));
                } else {
                    holder.itemView.setBackgroundColor(getResources().getColor(R.color.colorNoLove));
                }
                TextView text = (TextView) holder.itemView.findViewById(R.id.textView);
                ImageView view = (ImageView) holder.itemView.findViewById(R.id.imageView);
                text.setText(users.get(position).login);
                Picasso.with(MainActivity.this).load(users.get(position).avatar).into(view);
            }

            @Override
            public int getItemCount() {
                return users.size();
            }
        };
        rv.setAdapter(adapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://api.github.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .build();
                GitHubService service = retrofit.create(GitHubService.class);

                Subscription subscription = service.listUsers()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<List<User>>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                LOGGER.error("Error ", e);
                            }

                            @Override
                            public void onNext(List<User> users2) {
                                users.clear();
                                users.addAll(users2);
                                adapter.notifyDataSetChanged();
                                LOGGER.info("Success: " + users2);
                            }
                        });
                subscriptionList.add(subscription);
            }
        });

        final Button but = (Button) findViewById(R.id.button);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        subscriptionList = new SubscriptionList();
    }

    @Override
    protected void onStop() {
        super.onStop();
        subscriptionList.unsubscribe();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
