import jobs.DailyRegionDigestEmailJob;
import models.*;
import org.junit.Test;
import utils.DemoData;
import utils.UrlUtils;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.running;

public class DailyRegionDigestEmailJobTest {
    
    @Test
    public void getRegionUserDigests() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                DemoData.loadDemoData();
                
                User subscribingUser = User.findByEmailAddressAndPassword("james@demo.com", "password");
                
                Region region = Region.findByUrlFriendlyName(UrlUtils.getUrlFriendlyName(DemoData.CRESTED_BUTTE_COLORADO_REGION));

                RegionSubscription regionSubscription = new RegionSubscription(subscribingUser, region);
                regionSubscription.save();
                
                Route route = Route.findByUrlFriendlyName(region, UrlUtils.getUrlFriendlyName(DemoData.WEST_MAROON_PASS_ROUTE));
                
                User commentingUser = User.findByEmailAddressAndPassword("matt@demo.com", "password");
                
                Comment comment = new Comment(commentingUser, route, "new comment");
                comment.save();
                
                List<DailyRegionDigestEmailJob.RegionUserDigest> regionUserDigests = DailyRegionDigestEmailJob.getRegionUserDigests();
                assertThat(regionUserDigests.size()).isEqualTo(1);
                assertThat(regionUserDigests.get(0).user).isEqualTo(subscribingUser);
                assertThat(regionUserDigests.get(0).comments.size()).isEqualTo(1);
                assertThat(regionUserDigests.get(0).comments.get(0).value).isEqualTo("new comment");
                assertThat(regionUserDigests.get(0).comments.get(0).user).isEqualTo(commentingUser);
            }
        });
    }
    
}
