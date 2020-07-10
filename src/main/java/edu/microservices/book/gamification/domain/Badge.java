/******************************************************************************
 * Copyright (C) 2017 ShenZhen Powerdata Information Technology Co.,Ltd
 * All Rights Reserved.
 * 本软件为深圳博安达开发研制。未经本公司正式书面同意，其他任何个人、团体不得使用、
 * 复制、修改或发布本软件.
 *****************************************************************************/

package edu.microservices.book.gamification.domain;

/**
 * class summary
 * <p>
 * class detail description
 *
 * @author Siyi Lu
 * @since 2020/7/7
 */
public enum Badge {

    BRONZE_MULTIPLICATOR(100),
    SILVER_MULTIPLICATOR(500),
    GOLD_MULTIPLICATOR(999),

    FIRST_ATTEMPT(0),
    FIRST_WON(-1),
    LUCKY_NUMBER(36);

    private int score;

    private Badge(int score){
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}
