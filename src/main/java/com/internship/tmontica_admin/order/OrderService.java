package com.internship.tmontica_admin.order;

import com.internship.tmontica_admin.option.Option;
import com.internship.tmontica_admin.option.OptionDao;
import com.internship.tmontica_admin.order.model.request.OrderStatusReq;
import com.internship.tmontica_admin.order.model.response.OrderDetailResp;
import com.internship.tmontica_admin.order.model.response.OrderStatusLogResp;
import com.internship.tmontica_admin.order.model.response.Order_MenusResp;
import com.internship.tmontica_admin.order.model.response.OrdersByStatusResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderDao orderDao;
    private final OptionDao optionDao;

    // 주문 상태 변경 api(관리자)
    public void updateOrderStatusApi(int orderId, OrderStatusReq orderStatusReq){
//        String userId = JsonUtil.getJsonElementValue(jwtService.getUserInfo("userInfo"),"id");
//        // 관리자 권한 검사
//        String role = JsonUtil.getJsonElementValue(jwtService.getUserInfo("userInfo"),"role");
//        if(!role.equals(UserRole.ADMIN.toString())){
//            throw new UserException(UserExceptionType.INVALID_USER_ROLE_EXCEPTION);
//        }
        // TODO: 관리자 아이디 받아오기
        String userId = "admin";

        // orders 테이블에서 status 수정
        orderDao.updateOrderStatus(orderId, orderStatusReq.getStatus());
        // order_status_log 테이블에도 로그 추가
        OrderStatusLog orderStatusLog = new OrderStatusLog(orderStatusReq.getStatus(), userId, orderId);
        orderDao.addOrderStatusLog(orderStatusLog);
    }

    // 주문 상태별로 주문정보 가져오기 api(관리자)
    public List<OrdersByStatusResp> getOrderByStatusApi(String status) {
//        // 관리자 권한 검사
//        String role = JsonUtil.getJsonElementValue(jwtService.getUserInfo("userInfo"),"role");
//        if(!role.equals(UserRole.ADMIN.toString())){
//            throw new UserException(UserExceptionType.INVALID_USER_ROLE_EXCEPTION);
//        }

        List<Order> orders = orderDao.getOrderByStatus(OrderStatusType.valueOf(status).getStatus());
        List<OrdersByStatusResp> ordersByStatusResps = new ArrayList<>();
        for(Order order : orders){
            List<Order_MenusResp> menus = orderDao.getOrderDetailByOrderId(order.getId());
            for (Order_MenusResp menu : menus) {
                //메뉴 옵션 "1__1/4__2" => "HOT/샷추가(2개)" 로 바꾸는 작업
                if(!menu.getOption().equals("")){
                    String option = menu.getOption();
                    String convert = convertOptionStringToCli(option); // 변환할 문자열
                    menu.setOption(convert);
                }

                menu.setImgUrl("/images/".concat(menu.getImgUrl()));
            }
            OrdersByStatusResp obs = new OrdersByStatusResp(order.getId(), order.getOrderDate(), order.getPayment(),
                    order.getTotalPrice(), order.getUsedPoint(), order.getRealPrice(), order.getStatus(), order.getUserId(), menus);

            ordersByStatusResps.add(obs);
        }
        return ordersByStatusResps;
    }


    // 주문 상세정보 가져오기 api(관리자)
    public OrderDetailResp getOrderDetailApi(int orderId){
//        // 관리자 권한 검사
//        String role = JsonUtil.getJsonElementValue(jwtService.getUserInfo("userInfo"),"role");
//        if(!role.equals(UserRole.ADMIN.toString())){
//            throw new UserException(UserExceptionType.INVALID_USER_ROLE_EXCEPTION);
//        }

        Order order = orderDao.getOrderByOrderId(orderId);
        List<Order_MenusResp> menus = orderDao.getOrderDetailByOrderId(orderId);
        for (Order_MenusResp menu : menus) {
            //메뉴 옵션 "1__1/4__2" => "HOT/샷추가(2개)" 로 바꾸는 작업
            if(!menu.getOption().equals("")){
                String option = menu.getOption();
                String convert = convertOptionStringToCli(option); // 변환할 문자열
                menu.setOption(convert);
            }
            menu.setImgUrl("/images/".concat(menu.getImgUrl()));
        }
        List<OrderStatusLogResp> orderStatusLogs = orderDao.getOrderStatusLogByOrderId(orderId);

        OrderDetailResp orderDetailResp = new OrderDetailResp(order.getUserId(), orderId, order.getTotalPrice(),menus, orderStatusLogs);
        return orderDetailResp;
    }


    // DB의 옵션 문자열을 변환
    public String convertOptionStringToCli(String option){
        //메뉴 옵션 "1__1/4__2" => "HOT/샷추가(2개)" 로 바꾸는 작업
        StringBuilder convert = new StringBuilder();
        String[] arrOption = option.split("/");
        for (String opStr : arrOption) {
            String[] oneOption = opStr.split("__");
            Option tmpOption = optionDao.getOptionById(Integer.parseInt(oneOption[0]));

            if (tmpOption.getType().equals("Temperature")) {
                convert.append(tmpOption.getName());
            } else if(tmpOption.getType().equals("Shot")){
                convert.append("/샷추가("+oneOption[1]+"개)");
            } else if(tmpOption.getType().equals("Syrup")){
                convert.append("/시럽추가("+oneOption[1]+"개)");
            } else if(tmpOption.getType().equals("Size")){
                convert.append("/사이즈업");
            }
        }
        return convert.toString();
    }
}
