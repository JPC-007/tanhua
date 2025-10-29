package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.UserLocation;
import com.tanhua.domain.vo.UserLocationVo;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metric;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

/**
 * 搜附近服务接口实现类
 */
@Service
public class LocationApiImpl implements LocationApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 上报地理位置
     * latitude//纬度
     * longitude//经度
     * x表示当地的纬度，y表示当地的经度。
     * 113.929778,22.582111
     */
    @Override
    public void saveLocation(Long userId, Double latitude, Double longitude, String addrStr) {
        //a.根据userId查询地理位置表，看记录是否存在
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        UserLocation userLocation = mongoTemplate.findOne(query, UserLocation.class);
        long nowTime = System.currentTimeMillis();
        //b.如果不存在，保存地理位置
        if (userLocation == null) {
            userLocation = new UserLocation();
            userLocation.setUserId(userId);//上报用户的id
            userLocation.setAddress(addrStr);//上报用户位置信息
            userLocation.setCreated(nowTime);//上报地理位置初始时间
            userLocation.setUpdated(nowTime);//上报地理位置更新时间
            userLocation.setLastUpdated(nowTime);//上报地理位置更新时间
            if (latitude > longitude) {
                userLocation.setLocation(new GeoJsonPoint(latitude, longitude));
            } else {
                userLocation.setLocation(new GeoJsonPoint(longitude, latitude));
            }
            mongoTemplate.save(userLocation);
        } else {
            //c.如果存在，更新地理位置
            Update update = new Update();
            if (latitude > longitude) {
                update.set("location",new GeoJsonPoint(latitude, longitude));
            } else {
                update.set("location",new GeoJsonPoint(longitude, latitude));
            }
            update.set("address",addrStr);
            update.set("updated",nowTime);
            update.set("lastUpdated",nowTime);
            mongoTemplate.updateFirst(query,update,UserLocation.class);
        }
    }

    /**
     * 搜附近用户列表
     * @param userId
     * @param distance 米
     * @return
     */
    @Override
    public List<UserLocationVo> searchNearUser(Long userId, Long distance) {
        //a.根据当前用户id查询当前用户位置
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        UserLocation userLocation = mongoTemplate.findOne(query, UserLocation.class);
        GeoJsonPoint location = userLocation.getLocation();//当前用户位置
        //b.根据当前用户位置 与 距离 通过Mongodb的API搜附近用户列表数据

        Query nearQuery = new Query();
        //通过circle对象 封装当前用户位置 与 距离
        Circle circle = new Circle(location,new Distance(distance/1000, Metrics.KILOMETERS));
        nearQuery.addCriteria(Criteria.where("location").withinSphere(circle));
        List<UserLocation> userLocationList = mongoTemplate.find(nearQuery, UserLocation.class);
        return UserLocationVo.formatToList(userLocationList);
    }
}
