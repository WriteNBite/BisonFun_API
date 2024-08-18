package com.writenbite.bisonfun.api.service;

import com.writenbite.bisonfun.api.client.VideoContentModel;
import com.writenbite.bisonfun.api.database.entity.UserVideoContentStatus;
import lombok.Getter;
import org.apache.commons.lang3.IntegerRange;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.NonNull;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EpisodeStatusRelationHandler implements PairRelationHandler<Integer, UserVideoContentStatus> {
    private final Map<Range<Integer>, Set<UserVideoContentStatus>> allowedRangeStatusMap;

    public EpisodeStatusRelationHandler(@NonNull VideoContentModel videoContentModel) {
        allowedRangeStatusMap = getRangeStatusesByModel(videoContentModel);
    }

    @Override
    public Pair<Integer, UserVideoContentStatus> handleRelation(Integer watchedEpisodes, UserVideoContentStatus status, Priority priority){
        if(watchedEpisodes == null && status == null){
            return Pair.of(0, UserVideoContentStatus.PLANNED);
        } else if (watchedEpisodes == null) {
            return Pair.of(getWatchedEpisodesByStatus(status), status);
        } else if (status == null) {
            return Pair.of(watchedEpisodes, getStatusByWatchedEpisodes(watchedEpisodes));
        }

        Set<UserVideoContentStatus> set = getAllowedStatusesForEpisodes(watchedEpisodes);
        if (set.isEmpty()) {
            throw new IllegalArgumentException("Can't find proper relation for input");
        } else if(set.contains(status)){
            return Pair.of(watchedEpisodes, status);
        } else{
            return switch (priority) {
                case LEFT -> Pair.of(watchedEpisodes, getFirstStatusFromSet(set));
                case RIGHT -> Pair.of(getWatchedEpisodesByStatus(status), status);
                case null -> throw new IllegalArgumentException("Priority must not be null");
            };
        }
    }

    private Map<Range<Integer>, Set<UserVideoContentStatus>> getRangeStatusesByModel(VideoContentModel model) {
        Map<Range<Integer>, Set<UserVideoContentStatus>> answer = new LinkedHashMap<>();

        //Set allowed UserVideoContent statuses by external model status
        Set<UserVideoContentStatus> statusesByVideoContentStatus = switch (model.getStatus()) {
            case ONGOING, PAUSED ->
                    Set.of(UserVideoContentStatus.PLANNED, UserVideoContentStatus.WATCHING, UserVideoContentStatus.PAUSED, UserVideoContentStatus.DROPPED);
            case RELEASED ->
                    Set.of(UserVideoContentStatus.PLANNED, UserVideoContentStatus.WATCHING, UserVideoContentStatus.PAUSED, UserVideoContentStatus.DROPPED, UserVideoContentStatus.COMPLETE);
            case UPCOMING, RUMORED, PLANNED, UNKNOWN -> Set.of(UserVideoContentStatus.PLANNED);
            case CANCELED ->
                    model.getEpisodes() > 0 ? Set.of(UserVideoContentStatus.PLANNED, UserVideoContentStatus.WATCHING, UserVideoContentStatus.PAUSED, UserVideoContentStatus.DROPPED, UserVideoContentStatus.COMPLETE) : Set.of(UserVideoContentStatus.PLANNED);
        };

        //Set allowed statuses for zero episodes
        answer.put(IntegerRange.is(0), filterStatuses(WatchingStatusSet.ZERO.statusSet, statusesByVideoContentStatus));

        //Set allowed statuses for watched episodes
        if (Set.of(VideoContentModel.Status.ONGOING, VideoContentModel.Status.PAUSED).contains(model.getStatus()) && model.getEpisodes() > 0){
            answer.put(IntegerRange.of(1, model.getEpisodes()), filterStatuses(WatchingStatusSet.WATCHING.statusSet, statusesByVideoContentStatus));
        } else if (Set.of(VideoContentModel.Status.RELEASED, VideoContentModel.Status.CANCELED).contains(model.getStatus()) && model.getEpisodes() > 1) {
            answer.put(IntegerRange.of(1, model.getEpisodes()-1), filterStatuses(WatchingStatusSet.WATCHING.statusSet, statusesByVideoContentStatus));
        }

        //Set allowed statuses for watched every episode of a finished or canceled content
        Set<UserVideoContentStatus> finishedSet = filterStatuses(WatchingStatusSet.FINISHED.statusSet, statusesByVideoContentStatus);
        if(!finishedSet.isEmpty()){
            answer.put(IntegerRange.is(model.getEpisodes()), finishedSet);
        }

        return answer;
    }

    private Set<UserVideoContentStatus> getAllowedStatusesForEpisodes(int watchedEpisodes) {
        return allowedRangeStatusMap.entrySet().stream()
                .filter(entry -> entry.getKey().contains(watchedEpisodes))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toSet());
    }

    private UserVideoContentStatus getStatusByWatchedEpisodes(int watchedEpisodes) {
        return getFirstStatusFromSet(getAllowedStatusesForEpisodes(watchedEpisodes));
    }

    private UserVideoContentStatus getFirstStatusFromSet(Set<UserVideoContentStatus> statuses){
        return statuses
                .stream()
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalArgumentException("Status set is empty"));
    }

    private int getWatchedEpisodesByStatus(UserVideoContentStatus status) {
        return allowedRangeStatusMap.entrySet().stream()
                .filter(entry -> entry.getValue().contains(status))
                .min(Comparator.comparingInt(statuses -> statuses.getKey().getMinimum()))
                .map(entry -> entry.getKey().getMinimum())
                .orElseThrow(() -> new IllegalArgumentException("No proper range found for the given status"));
    }

    private Set<UserVideoContentStatus> filterStatuses(Set<UserVideoContentStatus> statusSet, Set<UserVideoContentStatus> allowedStatuses) {
        return statusSet.stream().filter(allowedStatuses::contains).collect(Collectors.toSet());
    }

    @Getter
    private enum WatchingStatusSet {
        ZERO(Set.of(UserVideoContentStatus.PLANNED, UserVideoContentStatus.WATCHING, UserVideoContentStatus.PAUSED, UserVideoContentStatus.DROPPED)),
        WATCHING(Set.of(UserVideoContentStatus.WATCHING, UserVideoContentStatus.PAUSED, UserVideoContentStatus.DROPPED)),
        FINISHED(Set.of(UserVideoContentStatus.COMPLETE));

        private final Set<UserVideoContentStatus> statusSet;

        WatchingStatusSet(Set<UserVideoContentStatus> statusSet) {
            this.statusSet = statusSet;
        }

    }
}
