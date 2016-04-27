package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.awt.event.ActionEvent;
import java.beans.EventHandler;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

public class Controller implements Initializable {
    class Point {
        public int x;
        public int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private int numberOfPoint = 0;
    private final Alert warningAlert = new Alert(Alert.AlertType.WARNING);
    private List<Point> points = new Vector();
    private List<Point> polyLine = new Vector();

    @FXML
    private Canvas Canvas;
    private GraphicsContext gc;

    @FXML
    private TextField TextFieldOfPoint;

    @FXML
    private Button Run;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initialDraw();
        setAllEventHandler();
    }

    private void initialDraw() {
        gc = Canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);
    }

    private void initialGraphicLine() {
        polyLine = convexHull(points);
        int i = 1;
        for (i = 1; i < polyLine.size(); i++) {
            drawLine(polyLine.get(i - 1), polyLine.get(i));
        }
        drawLine(polyLine.get(i - 1), polyLine.get(0));
    }

    private void initialGraphicPoint(int numberOfPoint) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, Canvas.getWidth(), Canvas.getHeight());
        for (int i = 0; i < numberOfPoint; i++) {
            points.add(new Point((int) ((Math.random()) * 450) + 50, (int) (Math.random() * 450) + 50));
            drawPoint(points.get(i).x, points.get(i).y);
        }

    }

    private void drawPoint(int x, int y) {
        gc.strokeLine(x, y, x, y);
    }

    private void drawLine(Point a, Point b) {
        gc.strokeLine(a.x, a.y, b.x, b.y);
    }

    private void resetList() {
        points.removeAll(points);
        polyLine.removeAll(polyLine);
    }

    private void setAllEventHandler() {
        Run.setOnAction(event -> {
            try {
                resetList();
                numberOfPoint = Integer.parseInt(TextFieldOfPoint.getText());
                initialGraphicPoint(numberOfPoint);
                initialGraphicLine();
            } catch (Exception e) {
                showWarningDialog("Error!", "Change Your Number Of Point and Try Again!");
            }
        });
    }

    private void showWarningDialog(String title, String content) {
        warningAlert.setTitle(title);
        warningAlert.setContentText(content);
        warningAlert.showAndWait();
    }

    public List<Point> convexHull(List<Point> vertices) {
        int sx, lx; // 左右範圍
        Point vcurr = null; // 目前拜訪的點
        Point vnext = null; // 下一次的選擇點

        // 取得左右範圍
        int i, x;
        lx = vertices.get(0).x;
        sx = vertices.get(0).x;
        for (i = 1; i < vertices.size(); i++) {
            x = vertices.get(i).x;
            if (x > lx) lx = x;
            if (x < sx) sx = x;
        }

        // 找最左的最上做為起點
        for (Point v : vertices) {
            if (v.x == sx && (vcurr == null || v.y > vcurr.y)) {
                vcurr = v;
            }
        }

        int dxs, dys; // 起始斜率
        int dxc, dyc; // 暫存斜率
        int dxt, dyt; // 測試斜率
        int lqc, lqt; // 長度平方(暫存/測試)
        int scmp;    // 斜率比較結果

        polyLine.add(vcurr);

        dys = 1;
        dxs = 0;
        while (vcurr.x <= lx) {
            dyc = -1;
            dxc = 0;
            lqc = 0;
            for (Point v : vertices) {
                if (v.x >= vcurr.x) {
                    dyt = v.y - vcurr.y;
                    dxt = v.x - vcurr.x;
                    if (compareSlope(dyt, dxt, dys, dxs) == -1) {
                        scmp = compareSlope(dyt, dxt, dyc, dxc);
                        lqt = dyt * dyt + dxt * dxt;
                        if (scmp >= 0) {
                            if (scmp > 0 || lqt > lqc) {
                                vnext = v;
                                dyc = dyt;
                                dxc = dxt;
                                lqc = lqt;
                            }
                        }
                    }
                }
            }

            if (vnext == null) break;
            dys = dyc;
            dxs = dxc;
            polyLine.add(vnext);
            vertices.remove(vnext);
            vcurr = vnext;
            vnext = null;
        }

        dys = 1;
        dxs = 0;
        while (vcurr.x > sx) {
            dyc = -1;
            dxc = 0;
            lqc = 0;
            for (Point v : vertices) {
                if (v.x < vcurr.x) {
                    dyt = v.y - vcurr.y;
                    dxt = v.x - vcurr.x;
                    if (compareSlope(dyt, dxt, dys, dxs) == -1) {
                        scmp = compareSlope(dyt, dxt, dyc, dxc);
                        lqt = dyt * dyt + dxt * dxt;
                        if (scmp >= 0) {
                            if (scmp > 0 || lqt > lqc) {
                                vnext = v;
                                dyc = dyt;
                                dxc = dxt;
                                lqc = lqt;
                            }
                        }
                    }
                }
            }

            if (vnext == null) break;
            dys = dyc;
            dxs = dxc;
            polyLine.add(vnext);
            vertices.remove(vnext);
            vcurr = vnext;
            vnext = null;
        }

        return polyLine;
    }

    public int compareSlope(int dy2, int dx2, int dy1, int dx1) {
        if (dx2 != 0 && dx1 != 0) {
            // 兩數都不是無限大或無限小
            double test = dy2 * dx1 - dy1 * dx2;
            return (int) Math.signum(test);
        } else {
            if (dx2 != 0 || dx1 != 0) {
                // 其中一個數是無限大或無限小
                if (dx2 == 0) {
                    return dy2 >= 0 ? 1 : -1; // m1 無限大或無限小
                } else {
                    return dy1 >= 0 ? -1 : 1; // m2 無限大或無限小
                }
            } else {
                // 兩個數都是無限大或無限小
                if (dy2 >= 0) {
                    return dy1 >= 0 ? 0 : 1;  // m1 無限大
                } else {
                    return dy1 >= 0 ? -1 : 0; // m1 無限小
                }
            }
        }
    }
}
