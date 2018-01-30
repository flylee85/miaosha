package com.loserbird.miaosha.controller;

import com.loserbird.miaosha.domain.MiaoshaOrder;
import com.loserbird.miaosha.domain.MiaoshaUser;
import com.loserbird.miaosha.domain.OrderInfo;
import com.loserbird.miaosha.redis.RedisService;
import com.loserbird.miaosha.result.CodeMsg;
import com.loserbird.miaosha.service.GoodsService;
import com.loserbird.miaosha.service.MiaoshaService;
import com.loserbird.miaosha.service.MiaoshaUserService;
import com.loserbird.miaosha.service.OrderService;
import com.loserbird.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
@RequestMapping("/miaosha")
public class MiaoshaController {

	@Autowired
    MiaoshaUserService userService;
	
	@Autowired
    RedisService redisService;
	
	@Autowired
    GoodsService goodsService;
	
	@Autowired
    OrderService orderService;
	
	@Autowired
    MiaoshaService miaoshaService;
	
	/**
	 * QPS:1306
	 * 5000 * 10
	 * */
    @RequestMapping("/do_miaosha")
    public String list(Model model,MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId) {
    	model.addAttribute("user", user);
    	if(user == null) {
    		return "login";
    	}
    	//判断库存
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
    	int stock = goods.getStockCount();
    	if(stock <= 0) {
    		model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
    		return "miaosha_fail";
    	}
    	//判断是否已经秒杀到了
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) {
    		model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
    		return "miaosha_fail";
    	}
    	//减库存 下订单 写入秒杀订单
    	OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
    	model.addAttribute("orderInfo", orderInfo);
    	model.addAttribute("goods", goods);
        return "order_detail";
    }
}
