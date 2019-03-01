package org.flowant.website.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
@AllArgsConstructor(staticName="of")
@NoArgsConstructor
@Table
public class SubItem {

    @NonNull
    @Id
    @Column("id")
    UUID identity;

    @NonNull
    @Column("si")
    List<IdScore> subItems;

    public static SubItem of(UUID indentity) {
        return of(indentity, new ArrayList<IdScore>());
    }

}