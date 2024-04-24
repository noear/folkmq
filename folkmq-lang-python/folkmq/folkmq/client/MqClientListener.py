from asyncio import log

from socketd.exception.SocketDExecption import SocketDAlarmException
from socketd.transport.core.Message import Message
from socketd.transport.core.Session import Session
from socketd.transport.core.listener.EventListener import EventListener

from folkmq.client.MqClient import MqClient
from folkmq.client.MqClientDefault import MqClientDefault
from folkmq.client.MqMessageReceived import MqMessageReceivedImpl
from folkmq.common.MqConstants import MqConstants


class MqClientListener(EventListener):
    def init(self, client:MqClientDefault):
        self._client = client

        self.do_on(MqConstants.MQ_EVENT_DISTRIBUTE, self.doOn_distribute)
        self.do_on(MqConstants.MQ_EVENT_REQUEST, self.doOn_request)

    def doOn_distribute(self, s:Session, m:Message):
        try:
            message = MqMessageReceivedImpl(self._client, s, m)
            self.onReceive(s,m,message,False)
        except Exception as e:
            log.warn("Client consume handle error, sid=" + m.sid(), e)

    def doOn_request(self, s:Session, m:Message):
        try:
            message = MqMessageReceivedImpl(self._client, s, m)
            self.onReceive(s,m,message,True)
        except Exception as e:
            log.warn("Client consume handle error, sid=" + m.sid(), e);

    def onReceive(self, s: Session, m: Message, message: MqMessageReceivedImpl, isRequest: bool):
        ...

    def on_open(self, session:Session):
        super().on_open(session)

        log.info("Client session opened, sessionId=" + session.session_id())

    def on_close(self, session: Session):
        super().on_close(session)

        log.info("Client session closed, sessionId=" + session.session_id())

    def on_error(self, session: Session, error: Exception):
        super().on_error(session, error)

        if isinstance(error, SocketDAlarmException):
            log.warn("Client error, sessionId=" + session.session_id(), error)
        else:
            log.warn("Client error, sessionId=" + session.session_id(), error)
