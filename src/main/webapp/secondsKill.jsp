<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2020-5-28
  Time: 下午 05:21
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
    <script type="text/javascript" src="jquery/jquery-2.1.1.min.js"></script>
</head>
<body>

<form action="res/doSecondsKill" method="post">
    <input type="hidden" name="id" value="10001">
    <a href="#">点击开始秒杀</a>
</form>


<script type="text/javascript">
    $("a").click(function () {
        $.ajax({
                type: "post",
                url: $("form").prop("action"),
                data: $("form").serialize(),
                success: function (res) {
                    if (res == "ok") {
                        alert("秒杀成功!");
                    } else {
                        alert(res);
                        $("a").prop("disabled", true);
                    }
                }

            }
        );
    });
</script>

</body>
</html>
