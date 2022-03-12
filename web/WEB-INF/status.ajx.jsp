<%@ page import="de.elbe5.request.RequestData" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    String status = rdata.getString("status");
    String presentTiles = rdata.getString("presentTiles");
    String fetchedTiles = rdata.getString("fetchedTiles");
    String errors = rdata.getString("errors");
%>
            <td><%=status%></td>
            <td><%=presentTiles%></td>
            <td><%=fetchedTiles%></td>
            <td><%=errors%></td>
            <script type="application/javascript">
                getStatus(1000);
            </script>


