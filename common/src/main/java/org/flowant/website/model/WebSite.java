package org.flowant.website.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Builder
@Data
@Accessors(chain=true)
@AllArgsConstructor
@RequiredArgsConstructor(staticName="of")
@NoArgsConstructor
@Table
public class WebSite implements HasIdentity {

    @NonNull
    @PrimaryKey("id")
    UUID identity;

    @Column("cc")
    Map<String, UUID> contentContainerIds; //TODO implement

    @Column("pt")
    List<TagCount> popularTagCounts; //TODO implement

}
