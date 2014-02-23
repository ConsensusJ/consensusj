angular.module('peers', []);

function PeerCtrl($scope) {
    // initialization
    $scope.peers = [];

    var socket = new SockJS('/stomp');
    var stompClient = Stomp.over(socket);

    stompClient.connect('', '', function(frame) {

        console.log('Connected ' + frame);

        var userName = frame.headers['user-name'];

        stompClient.subscribe("/user/queue/peers", function(message) {
            console.log("got list of online peers");
            $scope.$apply(function() {
                $scope.peers = angular.fromJson(message.body);
            });

        });

        stompClient.send("/app/listPeers", {});

    }, function(error) {
        console.log("STOMP protocol error " + error);
    });
}
