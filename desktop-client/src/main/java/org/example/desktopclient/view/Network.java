package org.example.desktopclient.view;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import javafx.util.Pair;
import org.example.desktopclient.controller.BlockchainController;
import org.example.desktopclient.controller.NodeController;
import org.example.desktopclient.model.Block;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Network extends StackPane {
    private final int sectionSpacing = 28;
    private BlockchainController blockchainController = BlockchainController.getInstance();
    private NodeController nodeController = NodeController.getInstance();

    public Network(Scene scene, StackPane root) {
        blockchainController.updateBlockchainSizeInBytes();
        blockchainController.updateMempoolSizeInBytes();
        blockchainController.updateConnectionsCount();
        blockchainController.updateLastBlockTimestamp();
        blockchainController.updateFeeAmount();
        blockchainController.updateTotalTransactions();

        nodeController.updateNeighbours();

        blockchainController.updateLastBlocks();

        // Start

        VBox content = new VBox();
        content.setPadding(new Insets(sectionSpacing, sectionSpacing, sectionSpacing, sectionSpacing));
        content.setSpacing(sectionSpacing - 2);
        content.getStyleClass().add("network-content");

        //

        HBox firstRowSections = new HBox();
        firstRowSections.setSpacing(sectionSpacing);
        firstRowSections.setAlignment(Pos.TOP_CENTER);
        firstRowSections.prefHeightProperty().bind(content.heightProperty().multiply(0.55));

        VBox blockchainSection = new VBox();
        blockchainSection.setPadding(new Insets(13, 22, 13, 22));
        blockchainSection.getStyleClass().add("network-section");
        blockchainSection.prefWidthProperty().bind(content.widthProperty().multiply(0.56));

        HBox blockchainSectionTitleLabels = new HBox();
        blockchainSectionTitleLabels.setAlignment(Pos.TOP_LEFT);

        Label blockchainSectionTitle = new Label("Blockchain");
        blockchainSectionTitle.getStyleClass().add("network-section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox blockchainStatusBox = new HBox();
        blockchainStatusBox.setSpacing(3);
        blockchainStatusBox.setAlignment(Pos.CENTER_RIGHT);
        Circle circle = new Circle();
        circle.setRadius(5);
        /*circle.setFill(blockchainController.isBlockchainSync() ? Color.GREEN : Color.RED);*/
        circle.fillProperty().bind(Bindings.createObjectBinding(() -> {
            return blockchainController.isBlockchainSync() ? Color.GREEN : Color.RED;
        }, blockchainController.getIsBlockchainSyncProperty()));
        Label blockchainStatus = new Label();
        blockchainStatus.textProperty().bind(Bindings.createStringBinding(() -> {
            boolean isSync = blockchainController.isBlockchainSync();
            return isSync ? "Synchronized" : "Out of sync";
        }, blockchainController.getIsBlockchainSyncProperty()));
        blockchainStatus.getStyleClass().add("network-section-status");
        blockchainStatusBox.getChildren().addAll(circle, blockchainStatus);

        blockchainSectionTitleLabels.getChildren().addAll(blockchainSectionTitle, spacer, blockchainStatusBox);

        // Blockchain section content

        GridPane blockchainSectionContent = new GridPane();
        VBox.setMargin(blockchainSectionContent, new Insets(10, 0, 0, 0));
        blockchainSectionContent.prefHeightProperty().bind(content.heightProperty().multiply(0.4));
        blockchainSectionContent.setHgap(10);
        blockchainSectionContent.setVgap(10);

        for (int i = 0; i < 3; ++i) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / 3);
            blockchainSectionContent.getColumnConstraints().add(column);
        }
        for (int i = 0; i < 2; ++i) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(50.0);
            blockchainSectionContent.getRowConstraints().add(row);
        }

        // Size box

        VBox sizeBox = new VBox();
        sizeBox.getStyleClass().add("network-section-content-box");
        sizeBox.setAlignment(Pos.TOP_CENTER);
        sizeBox.setSpacing(15);

        Label sizeLabel = new Label("Size");
        sizeLabel.getStyleClass().add("network-section-content-label");

        HBox sizeValueBox = new HBox();
        sizeValueBox.setSpacing(5);
        sizeValueBox.setAlignment(Pos.CENTER);
        Label sizeValue = new Label("0");
        sizeValue.textProperty().bind(Bindings.createStringBinding(() -> {
            String unit = blockchainController.getBlockchainSizeInBytesProperty().get();
                return (unit != null && !unit.isEmpty()) ? unit.split(" ")[0] : "";
            }, blockchainController.getBlockchainSizeInBytesProperty()
        ));

        sizeValue.getStyleClass().add("network-section-content-value");

        Label sizeUnit = new Label("MB");
        sizeUnit.textProperty().bind(Bindings.createStringBinding(() -> {
                String size = blockchainController.getBlockchainSizeInBytesProperty().get();
                return (size != null && !size.isEmpty()) ? size.split(" ")[1] : "";
            }, blockchainController.getBlockchainSizeInBytesProperty()
        ));
        sizeUnit.getStyleClass().add("network-section-content-unit");
        sizeValueBox.getChildren().addAll(sizeValue, sizeUnit);

        sizeBox.getChildren().addAll(sizeLabel, sizeValueBox);

        // Mempool box

        VBox mempoolBox = new VBox();
        mempoolBox.getStyleClass().add("network-section-content-box");
        mempoolBox.setAlignment(Pos.TOP_CENTER);
        mempoolBox.setSpacing(15);

        Label mempoolLabel = new Label("Mempool");
        mempoolLabel.getStyleClass().add("network-section-content-label");

        HBox mempoolValueBox = new HBox();
        mempoolValueBox.setSpacing(5);
        mempoolValueBox.setAlignment(Pos.CENTER);
        Label mempoolValue = new Label("0");
        mempoolValue.textProperty().bind(Bindings.createStringBinding(() -> {
            String size = blockchainController.getMempoolSizeInBytesProperty().get();
                return (size != null && !size.isEmpty()) ? size.split(" ")[0] : "";
            }, blockchainController.getMempoolSizeInBytesProperty()
        ));
        mempoolValue.getStyleClass().add("network-section-content-value");

        Label mempoolUnit = new Label("Bytes");
        mempoolUnit.textProperty().bind(Bindings.createStringBinding(() -> {
                String unit = blockchainController.getMempoolSizeInBytesProperty().get();
                return (unit != null && !unit.isEmpty()) ? unit.split(" ")[1] : "";
            }, blockchainController.getMempoolSizeInBytesProperty()
        ));
        mempoolUnit.getStyleClass().add("network-section-content-unit");
        mempoolValueBox.getChildren().addAll(mempoolValue, mempoolUnit);

        mempoolBox.getChildren().addAll(mempoolLabel, mempoolValueBox);

        // Connections box

        VBox connectionsBox = new VBox();
        connectionsBox.getStyleClass().add("network-section-content-box");
        connectionsBox.setAlignment(Pos.TOP_CENTER);
        connectionsBox.setSpacing(15);

        Label connectionsLabel = new Label("Connections");
        connectionsLabel.getStyleClass().add("network-section-content-label");

        HBox connectionsValueBox = new HBox();
        connectionsValueBox.setSpacing(5);
        connectionsValueBox.setAlignment(Pos.CENTER);
        Label connectionsValue = new Label();
        connectionsValue.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(blockchainController.getConnectionsCountProperty().get()), blockchainController.getConnectionsCountProperty()));
        connectionsValue.getStyleClass().add("network-section-content-value");

        Label connectionsUnit = new Label("Peers");
        connectionsUnit.getStyleClass().add("network-section-content-unit");
        connectionsValueBox.getChildren().addAll(connectionsValue, connectionsUnit);

        connectionsBox.getChildren().addAll(connectionsLabel, connectionsValueBox);

        // LastBlock box

        VBox lastBlockBox = new VBox();
        lastBlockBox.getStyleClass().add("network-section-content-box");
        lastBlockBox.setAlignment(Pos.TOP_CENTER);
        lastBlockBox.setSpacing(15);

        Label lastBlockLabel = new Label("Last Block");
        lastBlockLabel.getStyleClass().add("network-section-content-label");

        HBox lastBlockValueBox = new HBox();
        lastBlockValueBox.setAlignment(Pos.CENTER);
        lastBlockValueBox.setSpacing(5);
        Label lastBlockValue = new Label();
        lastBlockValue.textProperty().bind(Bindings.createStringBinding(() -> {
            String timestamp = blockchainController.getLastBlockTimestampProperty().get();
            return (timestamp != null && !timestamp.isEmpty()) ? timestamp.split(" ")[0] : "";
        }, blockchainController.getLastBlockTimestampProperty()));
        lastBlockValue.getStyleClass().add("network-section-content-value");

        Label lastBlockUnit = new Label();
        lastBlockUnit.textProperty().bind(Bindings.createStringBinding(() -> {
            String timestamp = blockchainController.getLastBlockTimestampProperty().get();
            return (timestamp != null && !timestamp.isEmpty()) ? timestamp.split(" ")[1] : "";
        }, blockchainController.getLastBlockTimestampProperty()));
        lastBlockUnit.getStyleClass().add("network-section-content-unit");
        lastBlockValueBox.getChildren().addAll(lastBlockValue, lastBlockUnit);

        lastBlockBox.getChildren().addAll(lastBlockLabel, lastBlockValueBox);

        // Fee box

        VBox feeBox = new VBox();
        feeBox.getStyleClass().add("network-section-content-box");
        feeBox.setAlignment(Pos.TOP_CENTER);
        feeBox.setSpacing(15);

        Label feeLabel = new Label("Fee");
        feeLabel.getStyleClass().add("network-section-content-label");

        HBox feeValueBox = new HBox();
        feeValueBox.setSpacing(5);
        feeValueBox.setAlignment(Pos.CENTER);
        Label feeValue = new Label();
        feeValue.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(blockchainController.getFeeAmountProperty().get()), blockchainController.getFeeAmountProperty()));
        feeValue.getStyleClass().add("network-section-content-value");

        Label feeUnit = new Label("m/byte");
        feeUnit.getStyleClass().add("network-section-content-unit");
        feeValueBox.getChildren().addAll(feeValue, feeUnit);

        feeBox.getChildren().addAll(feeLabel, feeValueBox);

        // Transactions box

        VBox transactionsBox = new VBox();
        transactionsBox.getStyleClass().add("network-section-content-box");
        transactionsBox.setAlignment(Pos.TOP_CENTER);
        transactionsBox.setSpacing(15);

        Label transactionsLabel = new Label("Transactions");
        transactionsLabel.getStyleClass().add("network-section-content-label");

        Label transactionsValue = new Label();
        transactionsValue.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(blockchainController.getTotalTransactionsProperty().get()), blockchainController.getTotalTransactionsProperty()));
        transactionsValue.getStyleClass().add("network-section-content-value");

        transactionsBox.getChildren().addAll(transactionsLabel, transactionsValue);

        //

        //blockchainSectionContent.getChildren().addAll(sizeBox, mempoolBox, connectionsBox, lastBlockBox, feeBox, transactionsBox);
        blockchainSectionContent.add(sizeBox, 0, 0);
        blockchainSectionContent.add(mempoolBox, 1, 0);
        blockchainSectionContent.add(connectionsBox, 2, 0);
        blockchainSectionContent.add(lastBlockBox, 0, 1);
        blockchainSectionContent.add(feeBox, 1, 1);
        blockchainSectionContent.add(transactionsBox, 2, 1);

        blockchainSection.getChildren().addAll(blockchainSectionTitleLabels, blockchainSectionContent);

        // Second Section (Peers)

        VBox peersSection = new VBox();
        peersSection.setPadding(new Insets(13, 22, 13, 22));
        peersSection.getStyleClass().add("network-section");
        peersSection.prefWidthProperty().bind(content.widthProperty().multiply(0.44));
        peersSection.setAlignment(Pos.TOP_LEFT);

        Label titlePeers = new Label("Peers");
        titlePeers.getStyleClass().add("network-section-title");

        // Peer table

        ObservableList<Pair<String, SimpleBooleanProperty>> peerData = FXCollections.observableArrayList(
                nodeController.getNeighbours()
                .entrySet()
                .stream()
                .map(e -> new Pair<String, SimpleBooleanProperty>(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));
        TableView<Pair<String, SimpleBooleanProperty>> peerTable = new TableView<>(peerData);
        peerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        peerTable.getStyleClass().add("network-section-table");

        Label noPeersLabel = new Label("No peers connected");
        noPeersLabel.getStyleClass().add("network-section-no-peers");
        peerTable.setPlaceholder(noPeersLabel);

        TableColumn<Pair<String, SimpleBooleanProperty>, String> peerColumn = new TableColumn<>("IP");
        peerColumn.setCellValueFactory(cellData -> {
            String ip = cellData.getValue().getKey().substring(cellData.getValue().getKey().indexOf("://") + "://".length(), cellData.getValue().getKey().lastIndexOf(":"));
            return new SimpleStringProperty(ip);
        });

        TableColumn<Pair<String, SimpleBooleanProperty>, String> portColumn = new TableColumn<>("Port");
        portColumn.setCellValueFactory(cellData -> {
            String port = cellData.getValue().getKey().substring(cellData.getValue().getKey().lastIndexOf(":") + 1);
            return new SimpleStringProperty(port);
        });

        TableColumn<Pair<String, SimpleBooleanProperty>, HBox> statusColumn = new TableColumn<>("");
        statusColumn.setCellValueFactory(cellData -> {
            boolean isOnline = cellData.getValue().getValue().get();
            HBox statusBox = new HBox();
            statusBox.setAlignment(Pos.CENTER);
            statusBox.setSpacing(2);

            Circle statusCircle = new Circle();
            statusCircle.setRadius(4);
            statusCircle.setFill(isOnline ? Color.GREEN : Color.RED);
            Label statusLabel = new Label();
            statusLabel.getStyleClass().add("network-section-peer-status");
            statusLabel.textProperty().bind(Bindings.createStringBinding(() -> {
                return isOnline ? "Online" : "Offline";
            }, cellData.getValue().getValue()));
            statusBox.getChildren().addAll(statusCircle, statusLabel);

            return Bindings.createObjectBinding(() -> statusBox, cellData.getValue().getValue());
        });

        peerTable.getColumns().addAll(peerColumn, portColumn, statusColumn);

        peersSection.getChildren().addAll(titlePeers, peerTable);

        // Latest blocks section

        VBox latestBlocksSection = new VBox();
        latestBlocksSection.setPadding(new Insets(13, 22, 13, 22));
        latestBlocksSection.getStyleClass().add("network-section");
        latestBlocksSection.prefWidthProperty().bind(content.widthProperty());
        latestBlocksSection.minHeightProperty().bind(content.heightProperty().multiply(0.4));

        Label titleLatestBlocks = new Label("Latest Blocks");
        titleLatestBlocks.getStyleClass().add("network-section-title");

        StackPane latestBlocksContent = new StackPane();
        latestBlocksContent.setAlignment(Pos.CENTER);
        latestBlocksContent.prefHeightProperty().bind(latestBlocksSection.heightProperty().multiply(0.8));
        latestBlocksContent.prefWidthProperty().bind(latestBlocksSection.widthProperty());

        Pane lineLayer = new Pane();
        Line timeline = new Line();
        timeline.setStroke(Color.BLACK);
        timeline.setStrokeWidth(2);

        timeline.startXProperty().set(0);
        timeline.endXProperty().bind(latestBlocksContent.widthProperty());
        timeline.startYProperty().bind(latestBlocksContent.heightProperty().multiply(.35));
        timeline.endYProperty().bind(latestBlocksContent.heightProperty().multiply(.35));

        lineLayer.getChildren().add(timeline);

        HBox blocksContainer = new HBox();
        blocksContainer.setAlignment(Pos.CENTER);

        // Block data

        for(Block block : blockchainController.getLastBlocks()) {
            VBox blockData = new VBox();
            blockData.setOnMouseClicked(e -> {
                if(e.getClickCount() == 2) {
                    BlockModal blockModal = new BlockModal(root, block);
                    blockModal.prefWidthProperty().bind(root.widthProperty().multiply(.7));
                    blockModal.prefHeightProperty().bind(root.heightProperty().multiply(.9));

                    BoxBlur blur = new BoxBlur(3, 3, 2);
                    for (Node node : root.getChildren()) {
                        node.setEffect(blur);
                    }

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), blockModal);
                    blockModal.setOpacity(0);
                    fadeIn.setToValue(1);

                    StackPane.setAlignment(blockModal, Pos.CENTER);
                    root.getChildren().addAll(blockModal);
                    fadeIn.play();

                    setOnKeyPressed(event -> {
                        if (event.getCode().toString().equals("ESCAPE")) {
                            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), blockModal);
                            fadeOut.setToValue(0);
                            fadeOut.setOnFinished(ev -> {
                                root.getChildren().removeAll(blockModal);
                                for (Node node : root.getChildren()) {
                                    node.setEffect(null);
                                }
                            });
                            fadeOut.play();
                        }
                    });
                }
            });
            blockData.setAlignment(Pos.CENTER);

            Image blockImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/block_icon.png")));
            ImageView blockImageView = new ImageView(blockImage);
            blockImageView.setFitWidth(73);
            blockImageView.setFitHeight(73);

            VBox blockDataBox = new VBox();
            blockDataBox.prefWidthProperty().bind(latestBlocksContent.prefWidthProperty().divide(5));
            blockDataBox.setAlignment(Pos.CENTER);
            blockDataBox.getStyleClass().add("network-latest-block-data-box");

            Label blockIndexLabel = new Label("Block #" + block.getIndex());
            blockIndexLabel.getStyleClass().add("network-latest-block-index");

            Label transactionCountLabel = new Label(block.getTransactions().size() + " transaction" + (block.getTransactions().size() != 1 ? "s" : ""));
            transactionCountLabel.getStyleClass().add("network-latest-block-transactions");

            Label timePassedLabel = new Label(blockchainController.getTimePassed(block.getTimeStamp()) + " ago");
            timePassedLabel.getStyleClass().add("network-latest-block-time");

            blockDataBox.getChildren().addAll(blockIndexLabel, transactionCountLabel, timePassedLabel);
            blockData.getChildren().addAll(blockImageView, blockDataBox);
            blocksContainer.getChildren().addAll(blockData);
        }

        ScrollPane blocksScrollPane = new ScrollPane(blocksContainer);
        blocksScrollPane.getStyleClass().add("network-latest-blocks-scroll-pane");
        blocksScrollPane.setFitToHeight(true);
        blocksScrollPane.setFitToWidth(false);
        blocksScrollPane.setHvalue(Double.MAX_VALUE);
        /*setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.LEFT) {
                blocksScrollPane.setHvalue(blocksScrollPane.getHvalue() - blocksScrollPane.getHvalue() / 5);
            }
            else if(e.getCode() == KeyCode.RIGHT) {
                blocksScrollPane.setHvalue(blocksScrollPane.getHvalue() + blocksScrollPane.getHvalue() / 5);
            }
        });*/
        blocksScrollPane.hvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() == 0.0) {
                blockchainController.updateLastBlocks(10);
                Platform.runLater(() -> {
                    blocksContainer.getChildren().clear();
                    for (Block block : blockchainController.getLastBlocks()) {
                        VBox blockData = new VBox();
                        blockData.setAlignment(Pos.CENTER);

                        Image blockImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/block_icon.png")));
                        ImageView blockImageView = new ImageView(blockImage);
                        blockImageView.setFitWidth(73);
                        blockImageView.setFitHeight(73);

                        VBox blockDataBox = new VBox();
                        blockDataBox.prefWidthProperty().bind(latestBlocksContent.prefWidthProperty().divide(5));
                        blockDataBox.setAlignment(Pos.CENTER);
                        blockDataBox.getStyleClass().add("network-latest-block-data-box");

                        Label blockIndexLabel = new Label("Block #" + block.getIndex());
                        blockIndexLabel.getStyleClass().add("network-latest-block-index");

                        Label transactionCountLabel = new Label(block.getTransactions().size() + " transaction" + (block.getTransactions().size() != 1 ? "s" : ""));
                        transactionCountLabel.getStyleClass().add("network-latest-block-transactions");

                        Label timePassedLabel = new Label(blockchainController.getTimePassed(block.getTimeStamp()) + " ago");
                        timePassedLabel.getStyleClass().add("network-latest-block-time");

                        blockDataBox.getChildren().addAll(blockIndexLabel, transactionCountLabel, timePassedLabel);
                        blockData.getChildren().addAll(blockImageView, blockDataBox);
                        blockData.setOnMouseClicked(e -> {
                            if(e.getClickCount() == 2) {
                                BlockModal blockModal = new BlockModal(root, block);
                                blockModal.prefWidthProperty().bind(root.widthProperty());
                                blockModal.prefHeightProperty().bind(root.heightProperty());

                                BoxBlur blur = new BoxBlur(3, 3, 2);
                                for (Node node : root.getChildren()) {
                                    node.setEffect(blur);
                                }

                                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), blockModal);
                                blockModal.setOpacity(0);
                                fadeIn.setToValue(1);

                                StackPane.setAlignment(blockModal, Pos.CENTER);
                                root.getChildren().addAll(blockModal);
                                fadeIn.play();

                                setOnKeyPressed(event -> {
                                    if (event.getCode().toString().equals("ESCAPE")) {
                                        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), blockModal);
                                        fadeOut.setToValue(0);
                                        fadeOut.setOnFinished(ev -> {
                                            root.getChildren().removeAll(blockModal);
                                            for (Node node : root.getChildren()) {
                                                node.setEffect(null);
                                            }
                                        });
                                        fadeOut.play();
                                    }
                                });
                            }
                        });
                        blocksContainer.getChildren().add(blockData);
                    }
                });
            }
        });
        blocksScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        blocksScrollPane.setPannable(true);

        latestBlocksContent.getChildren().addAll(lineLayer, blocksScrollPane);
        latestBlocksSection.getChildren().addAll(titleLatestBlocks, latestBlocksContent);

        //

        firstRowSections.getChildren().addAll(blockchainSection, peersSection);

        content.getChildren().addAll(firstRowSections, latestBlocksSection);

        getChildren().add(content);
    }
}
