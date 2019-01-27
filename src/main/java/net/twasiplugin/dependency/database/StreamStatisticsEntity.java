package net.twasiplugin.dependency.database;

import net.twasi.core.database.models.BaseEntity;
import org.mongodb.morphia.annotations.Entity;

@Entity(value = "streamtrackerdata", noClassnameStored = true)
public class StreamStatisticsEntity extends BaseEntity {



}
