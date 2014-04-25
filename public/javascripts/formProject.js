/** @jsx React.DOM */

var App = React.createClass({
  getInitialState: function(){
    return { 
      me : $.getJSON("/me")
    };
  },
  render : function(){
    return <div><Notification data={this.state} /><YourProject /><Projects /><Hackers /></div>;
  }
});

var Notification = React.createClass({
  accept : function(e){
    console.log("accept");
    e.preventDefault();
  },
  refuse : function(){
    console.log("refuse");
  },
  render : function(){
    console.log(this.props.data);
    var form = <Participation3/>;
    return <div className="notification">{form}</div>;
  }
});

var Participation1 = React.createClass({
  render : function(){
    return <div className="wrapper participation-1">
        <h1>Veux-tu participer au prochain Hackday ?</h1>
        <p className="details">Une petite réponse avant mercredi 23 avril et on serait ravi !</p>
        <div className="buttons">
                <a href="" className="button polygon" onClick={this.accept}>oh que oui !</a>
                <a href="" onClick={this.refuse}>non</a>
        </div>
    </div>;
  }
});

var Participation2 = React.createClass({
  render : function(){
    return <div className="wrapper participation-2">
        <h1>Très heureux de te compter parmi nous :-)</h1>
        <form>
            <label className="details">Qu'est-ce que tu voudrais faire ?</label>
            <input type="text" placeholder="arduino, fun, café ..."/>
            <input type="submit" className="button polygon" value="GO"/>
        </form>
    </div>;
  }
});

var Participation3 = React.createClass({
  render : function(){
    return <div className="wrapper participation-3">
        <h1>Très heureux de te compter parmi nous :-)</h1>
        <form>
            <label className="details">Qu'est-ce que tu voudrais faire ?</label>
            <input type="text" placeholder="arduino, fun, café ..."/>
            <input type="submit" className="button polygon" value="GO"/>
        </form>
    </div>
  }
});

var YourProject = React.createClass({
  getInitialState: function(){
    return { 
      status : 'none'
    };
  },
  switchTo: function (newStatus) {
    this.setState({status: newStatus});
  },
  render : function(){
    return <div>
      {this.state.status === 'none' ? <createButton cb={this.switchTo}/> : ''}
      {this.state.status === 'creating' ? <formProject cb={this.switchTo}/> : ''}
      {this.state.status === 'displaying' ? <detailProject cb={this.switchTo}/> : ''}
      {this.state.status === 'editing' ? <formProject cb={this.switchTo}/> : ''}
    </div>;
  }
});

var Projects = React.createClass({
  render : function(){
    return <div></div>;
  }
});

var Hackers = React.createClass({
  render : function(){
    return <div></div>;
  }
});

var createButton = React.createClass({
  onClick: function () {
    this.props.cb('creating');
    // go({create: true});
  },
  render: function () {
    return <div class="new-project">
      <button type="button" class="button polygon" onClick={this.onClick}>Crée ton projet</button>
    </div>
  }
});

var formProject = React.createClass({
  onSubmit: function () {
    var self = this;

    console.log('onSubmit', this.props, this.state);
    $.ajax({
      type: 'POST',
      url: '/projects',
      contentType: 'application/json; charset=utf-8',
      data: JSON.stringify(this.state)
    }).done(function (data) {
      self.props.cb('displaying');
      // go({create: false});
    });
  },
  handleName: function (event) {
    this.setState({name: event.target.value});
  },
  handleDescription: function (event) {
    this.setState({description: event.target.value});
  },
  handleQuote: function (event) {
    this.setState({quote: event.target.value});
  },
  render: function() {
    if (this.props.data) {
      this.setState(this.props.data);
    }

    return <div class="new-project-2">
      <h1>Crée ton projet</h1>
      <form onSubmit={this.onSubmit}>
        <input type="text" onChange={this.handleName} placeholder="Nom du projet"/>
        <textarea onChange={this.handleDescription} placeholder="Description"/>
        <input type="text" onChange={this.handleQuote} placeholder="Il faut venir dans mon équipe paske..."/>
        <button type="submit" class="button polygon">GO !</button>
      </form>
    </div>;
  }
});

var detailProject = React.createClass({
  onClick: function () {
    this.props.cb('editing');
    // go({create: true});
  },
  render: function () {
    return <div>
      Your project
    </div>
  }
});

var createApp = function(){
  return React.renderComponent(<App/>, document.getElementById("app"));
};
