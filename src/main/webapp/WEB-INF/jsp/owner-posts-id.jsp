<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/directive/taglib.jspf" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%@ include file="/WEB-INF/jspf/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/jspf/owner-profile-nav.jspf" %>

    <c:set var="post" value="${postInfo}"/>
    <c:set var="host" value="${postInfo.host}"/>

    <div class="container">
        <div class="row gutters d-flex justify-content-center">
            <div class="col-xl-9 col-lg-9 col-md-12 col-sm-12 col-12 mt-3">
                <div class="card">
                    <div class="card-header">
                        <h6 class="mb-2 text-success">Post</h6>
                    </div>
                    <div class="card-body">
                        <div class="row g-2">
                            <div class="col-xl-6 col-lg-6 col-md-6 col-sm-6 col-12">
                                <div class="form-col">
                                    <p><b>Host:</b> <c:out value="${host.name} ${host.surname}"/></p>
                                    <p><b>Pet:</b><span> <c:out value="${post.animal.name}"/></span></p>
                                    <p><b>Max term:</b><span> <c:out value="${post.maxDays}"/> days</span></p>
                                </div>
                            </div>
                            <div class="col-xl-6 col-lg-6 col-md-6 col-sm-6 col-12">
                                <div class="form-col">
                                    <p><b>Address:</b> <c:out value="${host.city}"/>, <c:out value="${host.region}"/></p>
                                    <p><b>Phone:</b><span> <c:out value="${host.phone}"/></span></p>
                                    <div class="form-group">
                                        <label for="rating"><b>Rating: <c:out value="${host.rating}"/></b></label>
                                        <div id="rating" class="form-control-static">
                                            <script>
                                                $(function () {
                                                    $("#rating").rateYo({
                                                        rating: <c:out value="${host.rating}"/>
                                                    });
                                                    $("#rating").rateYo('option', 'readOnly', true);
                                                });
                                            </script>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-xl-12 col-lg-12 col-md-12 col-sm-12 col-12">
                                <div class="mb-2 text-right">
                                    <a href="/owner/createContract/${post.id}"><button type="button" id="submit" name="submit" class="btn btn-outline-success mx-1 mt-1">Reserve</button></a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
<footer class="container text-center py-3 pt-3 " id="foot">
    <div class="form-group text-center pb-0">
        <img src="https://cdn1.savepice.ru/uploads/2021/6/1/478a03651046359a001893198fcc996e-full.png"
             width="140" height="140">
        <p>&copy; 2021 FrogProg Inc.</p>
    </div>

</footer>
</body>
</html>