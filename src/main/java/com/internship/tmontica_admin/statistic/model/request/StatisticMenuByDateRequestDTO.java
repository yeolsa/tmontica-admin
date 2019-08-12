package com.internship.tmontica_admin.statistic.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter @Setter
public class StatisticMenuByDateRequestDTO extends StatisticRequestDTO {

    @NotNull
    private List<Integer> menuIdList;
}
